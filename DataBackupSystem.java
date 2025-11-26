import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Data Backup System with Version Management
 * Stores serialized objects inside user-home directory
 */
public class DataBackupSystem {

    // Directory inside user home (always safe)
    private static final String BACKUP_DIR =
            System.getProperty("user.home") + File.separator + "java_backups";

    // Metadata file storing version numbers
    private static final File METADATA_FILE =
            new File(BACKUP_DIR + File.separator + "metadata.dat");

    private Map<String, Integer> versionMap = new HashMap<>();

    public DataBackupSystem() {
        createBackupDir();
        loadMetadata();
    }

    // Create a guaranteed writable folder
    private void createBackupDir() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) dir.mkdirs();
        System.out.println("Backup Folder: " + dir.getAbsolutePath());
    }

    // Load version metadata
    @SuppressWarnings("unchecked")
    private void loadMetadata() {
        if (!METADATA_FILE.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(METADATA_FILE))) {
            versionMap = (Map<String, Integer>) ois.readObject();
        } catch (Exception e) {
            System.out.println("Failed to load metadata.");
        }
    }

    // Save version metadata
    private void saveMetadata() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(METADATA_FILE))) {
            oos.writeObject(versionMap);
        } catch (Exception e) {
            System.out.println("Error writing metadata.");
        }
    }

    // Create backup with auto-version
    public void createBackup(String name, Serializable object) {
        int version = versionMap.getOrDefault(name, 0) + 1;
        versionMap.put(name, version);

        String filename = name + "_v" + version + ".dat";
        File file = new File(BACKUP_DIR, filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(object);
            System.out.println("Backup created: " + filename);
            saveMetadata();
        } catch (Exception e) {
            System.out.println("Error creating backup.");
        }
    }

    // List all backups
    public void listBackups() {
        if (versionMap.isEmpty()) {
            System.out.println("No backups found.");
            return;
        }

        System.out.println("==== Available Backups ====");
        versionMap.forEach((k, v) -> {
            System.out.println(k + " → Versions: " + v);
        });
    }

    // Restore a specific version
    public Object restoreBackup(String name, int version) {
        if (!versionMap.containsKey(name)) {
            System.out.println("No backup found.");
            return null;
        }

        String filename = name + "_v" + version + ".dat";
        File file = new File(BACKUP_DIR, filename);

        if (!file.exists()) {
            System.out.println("Version does not exist.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            System.out.println("Restored: " + filename);
            return obj;
        } catch (Exception e) {
            System.out.println("Error restoring backup.");
            return null;
        }
    }

    // Menu
    public static void main(String[] args) {
        DataBackupSystem backupSystem = new DataBackupSystem();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== DATA BACKUP SYSTEM ===");
            System.out.println("1. Create Backup");
            System.out.println("2. List Backups");
            System.out.println("3. Restore Backup");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {
                case 1 -> {
                    System.out.print("Enter object name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter data (string): ");
                    String data = sc.nextLine();

                    backupSystem.createBackup(name, new SampleData(data));
                }

                case 2 -> backupSystem.listBackups();

                case 3 -> {
                    System.out.print("Enter object name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter version number: ");
                    int v = sc.nextInt();

                    Object obj = backupSystem.restoreBackup(name, v);
                    System.out.println("Restored object: " + obj);
                }

                case 4 -> {
                    System.out.println("Exiting...");
                    return;
                }

                default -> System.out.println("Invalid option.");
            }
        }
    }
}

// A simple serializable object
class SampleData implements Serializable {
    private String value;

    public SampleData(String value) {
        this.value = value;
    }

    public String toString() {
        return "SampleData{value='" + value + "'}";
    }
}

