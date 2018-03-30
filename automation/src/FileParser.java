import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileParser {

    static int[] seeds = {1, 24, 351, 974, 63, 514, 3252};
    static int[] time_of_sims = {10800, 21600, 43200};                                              // 0-2
    static int[] hosts_amount = { 100, 250, 500};                                                   // 0-2
    static String[] movement_model = {"MBM", "SPMBM"};          // 0-1
    static String[] protocols = {"Epidemic", "FirstContact", "DirectDelivery",
            "Prophet", "SprayAndWait"};                                // 0-5
    static String[] names = {"delivery", "overhead", "latency"};

    static int curr_time_index = 0;
    static int curr_hosts_index = 0;
    static int curr_movement_index = 0;
    static int curr_protocol_index = 0;
    static int curr_seed_index = 0;


    public static void main(String[] args) {
        FileParser fileParser = new FileParser();

        for (; curr_protocol_index < protocols.length; curr_protocol_index++) {
            for (; curr_movement_index < movement_model.length; curr_movement_index++) {
                for (; curr_hosts_index < hosts_amount.length; curr_hosts_index++) {
                    for (; curr_time_index < time_of_sims.length; curr_time_index++) {
                        for (; curr_seed_index < seeds.length; curr_seed_index++) {
                            fileParser.processFile();
                        }
                        curr_seed_index = 0;
                    }
                    curr_time_index = 0;
                }
                curr_hosts_index = 0;
            }
            curr_movement_index = 0;
        }
    }


    public void processFile() {
        StringBuilder source_name = new StringBuilder("reports/");
        StringBuilder target_name = new StringBuilder("reports/");

        source_name.append(protocols[curr_protocol_index]);
        source_name.append("/");
        source_name.append(movement_model[curr_movement_index]);
        source_name.append("/");
        source_name.append(hosts_amount[curr_hosts_index]);
        source_name.append("n/");
        source_name.append(hosts_amount[curr_hosts_index]);
        source_name.append("n_");
        source_name.append(movement_model[curr_movement_index]);
        source_name.append("_");
        source_name.append(time_of_sims[curr_time_index]);
        source_name.append("s/");
        source_name.append(protocols[curr_protocol_index]);
        source_name.append("_");
        source_name.append(movement_model[curr_movement_index]);
        source_name.append("_");
        source_name.append(hosts_amount[curr_hosts_index]);
        source_name.append("n_");
        source_name.append(time_of_sims[curr_time_index]);
        source_name.append("s_");
        source_name.append(seeds[curr_seed_index]);
        source_name.append("r_MessageStatsReport.txt");

        target_name.append(protocols[curr_protocol_index]);
        target_name.append("/");
        target_name.append(movement_model[curr_movement_index]);
        target_name.append("/");
        target_name.append(hosts_amount[curr_hosts_index]);
        target_name.append("n/");
        target_name.append(hosts_amount[curr_hosts_index]);
        target_name.append("n_");
        target_name.append(movement_model[curr_movement_index]);
        target_name.append("_");
        target_name.append(time_of_sims[curr_time_index]);
        target_name.append("s/");
        target_name.append(protocols[curr_protocol_index]);
        target_name.append("_");
        target_name.append(movement_model[curr_movement_index]);
        target_name.append("_");
        target_name.append(hosts_amount[curr_hosts_index]);
        target_name.append("n_");
        target_name.append(time_of_sims[curr_time_index]);
        target_name.append("s_");


        try {
            FileInputStream inputStream = new FileInputStream(source_name.toString());
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            String contents = "";
            int line_number = 0;
            int last_index;
            String[] lineArray;

            file: while ((line = bufferedReader.readLine()) != null) {
                switch (line_number) {
                    case 9:
                        lineArray = line.split(" ");
                        contents = lineArray[1] + "\n";
                        target_name.append(names[0]);
                        writeToFile(contents, "Delivery\n", target_name.toString());
                        last_index = target_name.toString().lastIndexOf("_");
                        target_name.replace(last_index + 1, target_name.length(), "");
                        break;
                    case 11:
                        lineArray = line.split(" ");
                        contents = lineArray[1] + "\n";
                        target_name.append(names[1]);
                        writeToFile(contents, "Overhead\n", target_name.toString());
                        last_index = target_name.toString().lastIndexOf("_");
                        target_name.replace(last_index + 1, target_name.length(), "");
                        break;
                    case 12:
                        lineArray = line.split(" ");
                        contents = lineArray[1] + "\n";
                        target_name.append(names[2]);
                        writeToFile(contents, "Latency\n", target_name.toString());
                        last_index = target_name.toString().lastIndexOf("_");
                        target_name.replace(last_index + 1, target_name.length(), "");
                        break file;
                    default:
                        break;
                }
                line_number++;
            }
            inputStream.close();
            inputStreamReader.close();
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String content, String header, String path) {
        File tmpDir = new File(path);

        try {
            if (tmpDir.exists()) {
                Files.write(Paths.get(path), content.getBytes(), StandardOpenOption.APPEND);
            } else {
                FileOutputStream outputStream = new FileOutputStream(path);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

                bufferedWriter.write(header);
                bufferedWriter.write(content);

                bufferedWriter.close();
                System.out.println("Wrote to: " + path);
            }
        } catch (IOException e) {
            System.out.println("Problem occurs when checking the directory : " + path);
            e.printStackTrace();
        }
    }
}
