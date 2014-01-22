package com.codahale.metrics.resteasy;

import com.codahale.metrics.Timer;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class TimedInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    private final Timer timer;
    private Timer.Context context;

    public TimedInterceptor(Timer timer) {
        this.timer = timer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        context = timer.time();
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        context.stop();
    }
}
