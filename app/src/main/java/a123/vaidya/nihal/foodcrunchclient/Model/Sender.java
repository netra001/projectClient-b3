package a123.vaidya.nihal.foodcrunchclient.Model;

public class Sender {
    public String to;
    public Notification notification;

    public Sender(String token, Notification notification) {
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
