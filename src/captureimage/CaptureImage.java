package captureimage;

public class CaptureImage{
    public static void main(String[] args) {
        ScreenWindow sw=new ScreenWindow();
        sw.Capture(args[0]);
    }
}