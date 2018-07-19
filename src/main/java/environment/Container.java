package environment;

import utils.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.ProcessBuilder.Redirect.*;

public class Container {

    public final int uid;

    final String[] commands;

    Process process;


    Container(int _uid) {

        uid = _uid;



        commands = new String[] {

                // java
                System.getProperty("java.home") + "/bin/java".replace(':', '.'),

                // -classpath
                "-cp", System.getProperty("java.class.path"),

        };
    }

    public static void main(String[] args) {

        Path workingDir = Paths.get(System.getProperty("user.dir"));

        System.out.println(workingDir);
    }

//    public CompletableFuture<Container> start() {
//
//        return CompletableFuture.supplyAsync(() -> {
//
//            ProcessBuilder builder = new ProcessBuilder();
//
//            Path workingDir = Paths.get(System.getProperty("user.dir"));
//
//            checkState(Files.isDirectory(Paths.get(workingDir.filePath())));
//
//            File out = Paths.get(workingDir.filePath() + "/out").toFile();
//
//            try {
//
//                if (out.exists()) {
//
//                    checkState(out.delete());
//                }
//                if (out.createNewFile()) {
//
//                    Logger.info("redirect stdout to " + out.getPath());
//                }
//            }
//            catch (IOException ex) {
//
//                Logger.error(ex);
//            }
//
//            builder.redirectOutput(out);
//
//            builder.redirectError(Redirect.INHERIT);
//
//            builder.directory(Paths.get(workingDir.filePath()).toFile());
//
//            builder.command(commands);
//
//            try {
//
//                process = builder.start();
//
//                while (!process.isAlive()) {
//
//                    try {
//
//                        Thread.sleep(50);
//
//                    } catch (InterruptedException e) {
//
//                        e.printStackTrace();
//                    }
//                }
//            }
//            catch (IOException ex) {
//
//                Logger.error("error booting local container", ex);
//
//                return null;
//            }
//
//            return this;
//        });
//    }

    public CompletableFuture<Void> stop() {

        return CompletableFuture.runAsync(() -> {

            if (process != null && process.isAlive()) {

                process.destroyForcibly();

                try {

                    while (process.isAlive()) {

                        Thread.sleep(100);
                    }
                } catch (InterruptedException ex) {
                    // ignore.
                }
            }
        });
    }


}
