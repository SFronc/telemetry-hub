package com.sfronc.hub.server.dispatch;

import com.sfronc.hub.common.Ids;
import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.messages.ErrorResponse;
import com.sfronc.hub.common.protocol.FrameCodec;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;
import com.sfronc.hub.server.auth.AuthStrategy;
import com.sfronc.hub.server.exceptions.ServiceUnavailableException;
import com.sfronc.hub.server.exceptions.UnauthorizedException;
import com.sfronc.hub.server.metrics.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RequestDispatcher {
    private static final Logger log = LoggerFactory.getLogger(RequestDispatcher.class);

    private final CommandFactory factory;
    private final MetricsRegistry metrucs;
    private final AuthStrategy auth;

    private final List<Middleware> chain;

    public RequestDispatcher(CommandFactory factory, AuthStrategy auth, MetricsRegistry metrics) {
        this.factory = factory;
        this.auth = auth;
        this.metrucs = metrics;
        this.chain = List.of(
                new LoggingMiddleware(),
                new ValidationMiddleware()
        );
    }

    public CompletableFuture<MessageEnvelope> handle(byte[] frame) {
        return CompletableFuture.supplyAsync(() -> {
            MessageEnvelope env = FrameCodec.decode(frame);

            String cid = (env.correlationId() == null || env.correlationId().isBlank()) ? Ids.correlationId() : env.correlationId();

            env = new MessageEnvelope(env.type(), cid, env.payload(), env.authToken());

            try {
                MiddlewareContext ctx = new MiddlewareContext(env);

                for(Middleware m : chain) {
                    m.apply(ctx);
                }

                auth.authorize(env);

                Command cmd = factory.create(env);

                return cmd.execute();
            }
            catch (UnauthorizedException e) {
                metrucs.errorsIncrement();
                return error(env, "UNAUTHORIZED", e.getMessage());
            }
            catch (ServiceUnavailableException e) {
                metrucs.errorsIncrement();
                return error(env, "SERVICE_UNAVAILABLE", e.getMessage());
            }
            catch (Exception e) {
                metrucs.errorsIncrement();
                log.info("Request failed: type={} cid={} err={}", env.type(), cid, e.toString());
                return error(env, "BAD_REQUEST", e.getMessage());
            }
        });
    }

    private static MessageEnvelope error(MessageEnvelope env, String code, String msg){
        var err = new ErrorResponse(env.correlationId(), code, msg);
        return new MessageEnvelope(MessageType.ERROR, env.correlationId(), Json.toTree(err), null);
    }


    public interface Middleware {
        void apply(MiddlewareContext ctx);
    }

    public static final class MiddlewareContext {
        public final MessageEnvelope env;
        MiddlewareContext(MessageEnvelope env) { this.env = env; }
    }

    static final class LoggingMiddleware implements Middleware {
        @Override
        public void apply(MiddlewareContext ctx) {
            log.debug("Inbound type={} cid={}", ctx.env.type(), ctx.env.correlationId());
        }
    }

    static final class ValidationMiddleware implements Middleware {
        @Override
        public void apply(MiddlewareContext ctx) {
            if (ctx.env.type() == null) throw new IllegalArgumentException("type is required");
            if (ctx.env.payload() == null && ctx.env.type() != MessageType.PING) {
                throw new IllegalArgumentException("payload is required for " + ctx.env.type());
            }
        }
    }
}

