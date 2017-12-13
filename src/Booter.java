import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Booter extends JFrame {
    //Panel list: 0 menu panel,1 log panel
    public static List<JPanel> panelList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Booter booter = new Booter();
        booter.setTitle("贪吃蛇计分版");
        booter.setSize(300, 300);
        booter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MenuPanel main = new MenuPanel(booter);
        LogPanel log = new LogPanel(booter);
        panelList.add(main);
        panelList.add(log);

        booter.add(main);
//        booter.changePanel(0);

        booter.setVisible(true);
    }

    public void changePanel(int number) {
        for (JPanel panel : panelList) {
//            ((ChangeAblePanel) panel).myShow();
            panel.setVisible(false);
        }

        try {
            JPanel panel = panelList.get(number);
            ((ChangeAblePanel) panel).myShow();

            this.getContentPane().removeAll();
            this.getContentPane().add(panel);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Booter.changePanel: 调用出错");
        }
    }
}

interface ChangeAblePanel {
    void myShow();
}

class PanelUtil {
    public static void flatButton(JButton btn) {

    }
}

class MenuPanel extends JPanel implements ChangeAblePanel {
    private Booter booter;

    public MenuPanel(Booter booter) {
        this.booter = booter;

        //Buttons
        JButton start = new JButton("开始游戏");
        PanelUtil.flatButton(start);
        this.add(start);
        start.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Start game");

                //Creating the window with all its awesome snaky features
                Window f1 = new Window();
                booter.setVisible(false);

                //Setting up the window settings
                f1.setTitle("贪吃蛇计分版");
                f1.setSize(300, 300);
                f1.setVisible(true);
                f1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                //Listening exit
                f1.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        booter.setVisible(true);

                        try {
                            //Store score
                            DBHelper.getInstance().addLog("Gao WenQi", f1.getScore());
                            System.out.println("游戏结束,分数: " + f1.getScore());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });

        JButton log = new JButton("得分记录");
        PanelUtil.flatButton(log);
        this.add(log);
        log.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Show logger");
                booter.changePanel(1);
            }
        });
    }

    @Override
    public void myShow() {
        //TODO: Check data
        this.setVisible(true);
    }
}

class DBHelper {
    private static Connection conn = null;
    private static DBHelper dbHelper;

    public static DBHelper getInstance() throws Exception {
        if (dbHelper == null) {
            init();
        }
        return dbHelper;
    }

    private static void init() throws Exception {
        //Init sql
        conn =
                DriverManager.getConnection("jdbc:mysql://localhost:3306/gao" +
                        "?user=root&password=qwer1234&characterEncoding=utf8&serverTimezone=UTC");
        dbHelper = new DBHelper();
    }

    public void addLog(String name, int score) {
        try (Statement stmt = conn.createStatement()) {
            String sql = "insert into snake_log(name,score)" +
                    " values('" + name +
                    "','" + score + "')";
            System.out.println(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllLogs() {
        List<String> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM snake_log ORDER BY id DESC LIMIT 9");
            while (rs.next()) {
                //String id = rs.getString("id");
                String name = rs.getString("name");
                String score = rs.getString("score");

                result.add("名字: " + name + " 分数: " + score);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }
}

class LogPanel extends JPanel implements ChangeAblePanel {
    private Booter booter;
    private List<String> logs;
    private JLabel logShow;

    public LogPanel(Booter booter) throws Exception {
        this.booter = booter;
        this.logs = new ArrayList<>();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel text = new JLabel("游戏记录");
        logShow = new JLabel("");
        JButton backToMenu = new JButton("返回主菜单");
        PanelUtil.flatButton(backToMenu);

        this.add(text);
        this.add(backToMenu);
        this.add(logShow);

        backToMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("返回主菜单");
                booter.changePanel(0);
            }
        });


    }

    @Override
    public void myShow() {
        //logShow.setText("<html><br/> Hello <br/> Hi <br/> Fuck <br/></html>");
        try {
            String result = "<html>";
            List<String> logs = DBHelper.getInstance().getAllLogs();
            for (String str : logs) {
                result += (str + "<br/>");
            }
            result += "</html>";
            logShow.setText(result);
        } catch (Exception e) {
            e.printStackTrace();
            logShow.setText("获取数据库信息失败");
        }

        this.setVisible(true);
    }
}
