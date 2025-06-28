package world.novium.creative.backend;

import com.google.inject.Inject;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;
import world.novium.creative.common.managers.WorldManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WorldsEndpoint implements EndpointGroup {

    @Inject
    private WorldManager worldManager;

    @Override
    public void addEndpoints() {
        ApiBuilder.get("/worlds", ctx -> {
            // Logic to list all worlds
            ctx.result("List of worlds");
        });

        ApiBuilder.get("/worlds/:userId", ctx -> {
            try {
                UUID uuid = UUID.fromString(ctx.pathParam("userId"));
                File worldFolder = worldManager.getWorldFolder(uuid);

                if (!worldFolder.exists() || !worldFolder.isDirectory()) {
                    ctx.status(HttpStatus.NOT_FOUND).result("World not found");
                    return;
                }

                File zipFile = zipWorldFolder(worldFolder);

                ctx.header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"");
                ctx.contentType("application/zip");
                ctx.result(Files.newInputStream(zipFile.toPath()));

            } catch (IllegalArgumentException e) {
                ctx.status(HttpStatus.BAD_REQUEST).result("Invalid UUID");
            } catch (IOException e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Failed to zip world");
                e.printStackTrace();
            }
        });


    }

    private File zipWorldFolder(File folder) throws IOException {
        File zipFile = File.createTempFile("world-", ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Path folderPath = folder.toPath();

            Files.walk(folderPath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                try {
                    String entryName = folderPath.relativize(path).toString();
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }

        return zipFile;
    }
}