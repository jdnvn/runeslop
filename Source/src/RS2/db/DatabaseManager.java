package RS2.db;
import RS2.Settings;

public class DatabaseManager {
    private static Database instance = null;
    
    private DatabaseManager() {}
    
    public static Database getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = createDatabase();
                }
            }
        }
        return instance;
    }
    
    private static Database createDatabase() {
        switch (Settings.DB_TYPE) {
            case FILE:
                return new FileDatabase();
            case SQLITE:
                return new SqliteDatabase();
            default:
                throw new IllegalArgumentException("Unknown database type: " + Settings.DB_TYPE);
        }
    }
}
