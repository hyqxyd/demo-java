package cn.fighter3;

public class MessageCallback {
    private String message;
    public void onMessageReceived(String message) {
        this.message=message;
        System.out.println("接收到的消息: " + message);

    }
    public String getMessage() {
        return message;
    }
}
