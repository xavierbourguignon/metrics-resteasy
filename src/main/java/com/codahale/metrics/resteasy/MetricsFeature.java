package com.codahale.metrics.resteasy;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Joiner;

import javax.ws.rs.*;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static com.codahale.metrics.MetricRegistry.name;

@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class MetricsFeature implements DynamicFeature {

    private final MetricRegistry registry;

    public MetricsFeature(MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        Method resourceMethod = resourceInfo.getResourceMethod();
        if (resourceMethod.isAnnotationPresent(Timed.class)) {
            final Timed annotation = resourceMethod.getAnnotation(Timed.class);
            final String name = chooseName(annotation.name(), annotation.absolute(), resourceInfo);
            final Timer timer = registry.timer(name);
            context.register(new TimedInterceptor(timer));
        }

        if (resourceMethod.isAnnotationPresent(Metered.class)) {
            final Metered annotation = resourceMethod.getAnnotation(Metered.class);
            final String name = chooseName(annotation.name(), annotation.absolute(), resourceInfo);
            final Meter meter = registry.meter(name);
            context.register(new MeterInterceptor(meter));
        }
    }

    private String chooseName(String explicitName, boolean absolute, ResourceInfo resourceInfo) {
        if (explicitName != null && !explicitName.isEmpty()) {
            if (absolute) {
                return explicitName;
            }
            return name(getName(resourceInfo), explicitName);
        }

        return getName(resourceInfo);
    }

    private String getName(ResourceInfo resourceInfo) {
        return getMethod(resourceInfo.getResourceMethod()) + " - " + getPath(resourceInfo);
    }

    private String getPath(ResourceInfo resourceInfo) {
        String rootPath = null;
        String methodPath = null;

        if (resourceInfo.getResourceClass().isAnnotationPresent(Path.class)) {
            rootPath = resourceInfo.getResourceClass().getAnnotation(Path.class).value();
        }

        if (resourceInfo.getResourceMethod().isAnnotationPresent(Path.class)) {
            methodPath = resourceInfo.getResourceMethod().getAnnotation(Path.class).value();
        }

        return Joiner.on("/").skipNulls().join(rootPath, methodPath);
    }

    private String getMethod(Method resourceMethod) {
        if (resourceMethod.isAnnotationPresent(GET.class)) {
            return HttpMethod.GET;
        }
        if (resourceMethod.isAnnotationPresent(POST.class)) {
            return HttpMethod.POST;
        }
        if (resourceMethod.isAnnotationPresent(PUT.class)) {
            return HttpMethod.PUT;
        }
        if (resourceMethod.isAnnotationPresent(DELETE.class)) {
            return HttpMethod.DELETE;
        }
        if (resourceMethod.isAnnotationPresent(HEAD.class)) {
            return HttpMethod.HEAD;
        }
        if (resourceMethod.isAnnotationPresent(OPTIONS.class)) {
            return HttpMethod.OPTIONS;
        }

        throw new IllegalStateException("Resource method without GET, POST, PUT, DELETE, HEAD or OPTIONS annotation");
    }
}
