package de.buuddyyy.birnaloniasystem.handlers;

public final class TeleportHandler {

    private static TeleportHandler instance;

    public static TeleportHandler getInstance() {
        if (instance == null) {
            instance = new TeleportHandler();
        }
        return instance;
    }

    private TeleportHandler() {
    }



}
