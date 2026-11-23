package metallurgy;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.util.Arrays;

/** Точка входа: регистрация, вход и создание окна приложения. */
public class Main {
    public static void main(String[] args){SwingUtilities.invokeLater(Main::start);}
    private static void start(){
        try {
            Path folder=Path.of(System.getProperty("user.home"),"MetallurgCalculator");Files.createDirectories(folder);
            Database db=new Database(folder.resolve("metallurg.sqlite").toString());
            JTextField login=new JTextField(18);JPasswordField pass=new JPasswordField(18);
            JPanel p=new JPanel(new GridLayout(0,2,6,6));p.add(new JLabel("Логин (латиница)"));p.add(login);p.add(new JLabel("Пароль (от 8 символов)"));p.add(pass);
            while(true){int choice=JOptionPane.showOptionDialog(null,p,"Калькулятор металлурга — вход",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,new String[]{"Войти","Регистрация","Выход"},"Войти");if(choice==2||choice<0){db.close();return;}char[] secret=pass.getPassword();try{if(choice==1){db.register(login.getText().trim(),secret);JOptionPane.showMessageDialog(null,"Регистрация выполнена. Теперь войдите.");}else {long user=db.login(login.getText().trim(),secret);pass.setText("");new MainWindow(db,user,login.getText().trim()).setVisible(true);return;}}catch(Exception ex){JOptionPane.showMessageDialog(null,"Не удалось выполнить действие: "+ex.getMessage(),"Ошибка",JOptionPane.ERROR_MESSAGE);}finally{Arrays.fill(secret,'\0');}}
        }catch(Exception ex){JOptionPane.showMessageDialog(null,"Ошибка запуска: "+ex.getMessage(),"Ошибка",JOptionPane.ERROR_MESSAGE);}
    }
}
