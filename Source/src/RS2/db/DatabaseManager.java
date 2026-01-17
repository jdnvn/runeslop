package RS2.db;
import RS2.Settings;

public class DatabaseManager {
    public static Database createDatabase() {
        switch (Settings.DB_TYPE) {
            case FILE:
                return new FileDatabase();
            case SQLITE:
                return new SqliteDatabase();
            default:
                throw new IllegalArgumentException();
        }
    }
}
