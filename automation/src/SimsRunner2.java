import java.io.*;

public class SimsRunner2 {

    static int[] time_of_sims = {10800, 21600, 43200};
    static int[] hosts_amount = { 100, 250, 500};                                                       // 0-2
    static String[] movement_model = {"ShortestPathMapBasedMovement"};              // 0-1
    static String[] protocols = {"EpidemicRouter", "FirstContactRouter", "DirectDeliveryRouter",
            "MaxPropRouter", "ProphetRouter", "SprayAndWaitRouter"};                                    // 0-5

    static int curr_time_index = 0;
    static int curr_hosts_index = 0;
    static int curr_movement_index = 0;
    static int curr_protocol_index = 0;

    public static void main(String[] args) throws IOException, InterruptedException {

        for (; curr_protocol_index < protocols.length; curr_protocol_index++) {
            for (; curr_movement_index < movement_model.length; curr_movement_index++) {
                for (; curr_hosts_index < hosts_amount.length; curr_hosts_index++) {
                    for (; curr_time_index < time_of_sims.length; curr_time_index++) {
                        String file_to_process = SimsRunner1.processFiles();
                        ProcessBuilder pb = new ProcessBuilder(
                                "./one.sh",
                                "-b",
                                "" + SimsRunner1.NUMBER_OF_RUNS,
                                file_to_process
                        );
                        pb.directory(new File(System.getProperty("user.dir")));
                        pb.inheritIO();

                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                        System.out.println("Started: " + file_to_process.replaceFirst("generated_settings/", ""));
                        System.out.println("¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬");

                        Process process = pb.start();

                        if (process.isAlive()) {
                            System.err.println("Shouldn't happen");
                            process.destroyForcibly();
                        } else {
                            System.out.println("ALL GUCCI GANG");
                        }
                    }
                    curr_time_index = 0;
                }
                curr_hosts_index = 0;
            }
            curr_movement_index = 0;
        }
    }


}
