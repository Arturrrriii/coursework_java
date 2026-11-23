package metallurgy;

import java.sql.*;
import java.util.*;

/** SQLite: пользователи и история расчетов, изолированная по пользователю. */
public final class Database implements AutoCloseable {
    private final Connection c;
    public record Entry(long id, String time, ConverterCalculator.Input input) {}
    public Database(String file) throws SQLException {
        c = DriverManager.getConnection("jdbc:sqlite:" + file);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys=ON");
            s.execute("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY, login TEXT NOT NULL UNIQUE, salt BLOB NOT NULL, hash BLOB NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS calculations(id INTEGER PRIMARY KEY, user_id INTEGER NOT NULL REFERENCES users(id), created TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, mass REAL NOT NULL, depth REAL NOT NULL, intensity REAL NOT NULL, nozzles INTEGER NOT NULL, manganese REAL NOT NULL, angle REAL NOT NULL, slag REAL NOT NULL)");
        }
    }
    public void register(String login, char[] password) throws Exception {
        if (!login.matches("[a-zA-Z0-9_]{3,32}")) throw new IllegalArgumentException("Логин: 3–32 латинские буквы, цифры или знак _");
        if (password.length < 8) throw new IllegalArgumentException("Пароль должен содержать не менее 8 символов");
        byte[] salt=PasswordUtil.salt(), hash=PasswordUtil.hash(password,salt);
        try(PreparedStatement p=c.prepareStatement("INSERT INTO users(login,salt,hash) VALUES(?,?,?)")) {p.setString(1,login);p.setBytes(2,salt);p.setBytes(3,hash);p.executeUpdate();}
    }
    public long login(String login, char[] password) throws Exception {
        try(PreparedStatement p=c.prepareStatement("SELECT id,salt,hash FROM users WHERE login=?")) {p.setString(1,login);try(ResultSet r=p.executeQuery()){if(r.next() && PasswordUtil.verify(password,r.getBytes(2),r.getBytes(3))) return r.getLong(1);}}
        throw new IllegalArgumentException("Неверный логин или пароль");
    }
    public void insert(long user, ConverterCalculator.Input x) throws SQLException {
        ConverterCalculator.calculate(x);
        try(PreparedStatement p=c.prepareStatement("INSERT INTO calculations(user_id,mass,depth,intensity,nozzles,manganese,angle,slag) VALUES(?,?,?,?,?,?,?,?)")) {
            p.setLong(1,user);p.setDouble(2,x.mass());p.setDouble(3,x.depth());p.setDouble(4,x.intensity());p.setInt(5,x.nozzles());p.setDouble(6,x.manganese());p.setDouble(7,x.angle());p.setDouble(8,x.slagFactor());p.executeUpdate();
        }
    }
    public List<Entry> list(long user) throws SQLException {
        List<Entry> items = new ArrayList<>();
        try(PreparedStatement p=c.prepareStatement("SELECT id,created,mass,depth,intensity,nozzles,manganese,angle,slag FROM calculations WHERE user_id=? ORDER BY id DESC")) {p.setLong(1,user);try(ResultSet r=p.executeQuery()){while(r.next()) items.add(new Entry(r.getLong(1),r.getString(2),new ConverterCalculator.Input(r.getDouble(3),r.getDouble(4),r.getDouble(5),r.getInt(6),r.getDouble(7),r.getDouble(8),r.getDouble(9))));}}
        return items;
    }
    public void delete(long user, long id) throws SQLException {try(PreparedStatement p=c.prepareStatement("DELETE FROM calculations WHERE user_id=? AND id=?")){p.setLong(1,user);p.setLong(2,id);p.executeUpdate();}}
    public void close() throws SQLException {c.close();}
}
