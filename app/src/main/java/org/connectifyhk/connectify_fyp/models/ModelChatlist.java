package org.connectifyhk.connectify_fyp.models;

public class ModelChatlist {

    String id; // Get this id to chatlist for the users Uid

    public ModelChatlist(String id) {
        this.id = id;
    }

    public ModelChatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
