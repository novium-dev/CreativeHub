package world.novium.creative.backend;

import io.javalin.Javalin;
import world.novium.creative.CreativePlugin;

public class BackendServer implements Runnable {
    private final int port;
    private final String authToken;
    private final CreativePlugin plugin;
    private Javalin app;

    public BackendServer(int port, String authToken, CreativePlugin plugin) {
        this.port = port;
        this.authToken = authToken;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        app = Javalin.create(config -> config.router.apiBuilder(
                plugin.injector().getInstance(WorldsEndpoint.class)
        ));

        // check bearer auth
        app.before(ctx -> {
            String authHeader = ctx.header("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.status(401).result("Unauthorized");
                return;
            }
            String token = authHeader.substring(7);
            // Here you would validate the token, for now we just check if it's "valid-token"
            if (!authToken.equals(token)) {
                ctx.status(403).result("Forbidden");
            }
        });


        app.start(port);

    }

    public void stop() {
        if (app != null) {
            app.stop();
            app = null;
        }
    }
}