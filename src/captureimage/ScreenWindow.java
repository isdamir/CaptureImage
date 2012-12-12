/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package captureimage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 *
 * @author YuFeng
 */
public class ScreenWindow extends JFrame {

    private boolean isDrag = false;
    private int x = 0;
    private int y = 0;
    private int xE = 0;
    private int yE = 0;
    private String saveFilePath;
    public Dimension screenSize = null;  //屏幕大小
    public ImageIcon screenImage = null;
    public JLabel label = null;
    public JFrame mainWindow;

    public ScreenWindow() {
        this.mainWindow = new JFrame();
    }

    public void Capture(String filePath) {
        saveFilePath = filePath;
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        try {
            screenImage = new ImageIcon(ScreenImage.getScreenImage(0, 0, screenSize.width, screenSize.height));
        } catch (AWTException ex) {
            Logger.getLogger(ScreenWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ScreenWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        label = new JLabel(screenImage);
        label.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        //按键ESC退出执行
        KeyboardFocusManager keyManage = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keyManage.addKeyEventPostProcessor(new KeyEventPostProcessor() {
            @Override
            public boolean postProcessKeyEvent(KeyEvent e) {
                if (KeyEvent.VK_ESCAPE == e.getKeyCode()) { //判断是否按下ESC键
                    dispose();
                    mainWindow.dispose();
                    System.exit(0);
                }
                return false;
            }
        });

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    dispose();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                x = e.getX();
                y = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrag) {
                    xE = e.getX();
                    yE = e.getY();
                    if (x > xE) {
                        int temp = x;
                        x = xE;
                        xE = temp;
                    }
                    if (y > yE) {
                        int temp = y;
                        y = yE;
                        yE = temp;
                    }

                    //保存文件
                    try {
                        SaveFile();
                        dispose();
                        mainWindow.dispose();
                        System.exit(0);
                    } catch (Exception ex) {
                        int showConfirmDialog = JOptionPane.showConfirmDialog(null, "出现意外错误！", "系统提示", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        //鼠标拖曳事件   
        label.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDrag) {
                    isDrag = true;
                }
                //拖动过程的虚线选取框实现 
                int endx = e.getX();
                int endy = e.getY();
                BufferedImage bufferedimage = new BufferedImage(screenSize.width, screenSize.height, BufferedImage.TYPE_INT_BGR);
                Graphics g = bufferedimage.getGraphics();
                g.drawImage(screenImage.getImage(), 0, 0, screenSize.width, screenSize.height, null);
                g.setColor(Color.red);
                g.drawRect(x, y, endx - x, endy - y);
                label.setIcon(new ImageIcon(bufferedimage));
            }
        });
        this.setUndecorated(true);
        this.getContentPane().add(label);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        GraphicsDevice gd=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        } else {
            JOptionPane.showMessageDialog(null, "无法完全全屏");
        }
        this.setVisible(true);
    }

    void SaveFile() throws Exception {
        try {
            File file = new File(saveFilePath);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedImage bufferedimage = new BufferedImage(xE - x, yE - y, BufferedImage.TYPE_INT_BGR);
            Image image = ScreenImage.getScreenImage(x, y, xE - x, yE - y);
            Graphics g = bufferedimage.getGraphics();
            g.drawImage(image, 0, 0, xE - x, yE - y, null);
            g.setColor(Color.white);
            g.drawRect(0, 0, xE - x, yE - y);
            ImageIO.write(bufferedimage, "JPEG", fos);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public String RandomFileName(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}

class ScreenImage {
    //截图

    public static Image getScreenImage(int x, int y, int w, int h) throws AWTException, InterruptedException {
        Robot robot = new Robot();
        Image screen = robot.createScreenCapture(new Rectangle(x, y, w, h)).getScaledInstance(w, h, Image.SCALE_SMOOTH);
        MediaTracker tracker = new MediaTracker(new Label());
        tracker.addImage(screen, 1);
        tracker.waitForID(0);
        return screen;
    }
}