import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;



public class JobDispatcherClass {

    //***** GLOBAL VARIABLES ****
    public static int iTotalJobIDs;    // Total job IDs in the recommendation file
    public static int iExecutionRunID; // ID of the execution run currently in place
    public static List<String> IDs = new ArrayList<String>(); // List of job IDs in the recommendation file
    public static List<JobContainer> JobLibrary = new ArrayList<>(); // List of jobs listed in the library for a particular application

    //ALM specific config info
    public static String ALM_Server_Name = "ALM_Server_Name_From_Jenkins_Config"; // ALM server name listed in Jenkins server configuration
    public static String ALM_Username    = "coelf003";    // ALM user who can connect into ALM project where the job is located
    public static String ALM_Password    = "{AQAAABAAAAAQ6zNBkrfEAu5SrK6+XEp/BbzVuWj5myeHT3J/b1NkoZg=}";    // ALM user password to connect into ALM project to execute auto scripts



    /*
        Function to read CSV file with single column listing script IDs.
        The 1st row in the CSV file is IGNORED as it corresponds to the header.
        Sample:
                Script ID
                11
                12
                13
                15

        Author: Fernanda Menks - Feb 20, 2017
     */
    public List<String> ReadScriptIDs(){
        String csvFile = "./CSVs/JobIDs.csv";
        BufferedReader br = null;
        String line = "\n";
        String cvsSplitBy = ",";

        try {
            br = new BufferedReader(new FileReader(csvFile));
            // Ignore header line from CSV file
            br.readLine();

            // Add all script IDs into array 'ScriptIDs'
            while ((line = br.readLine()) != null) {

                String [] temp = line.split(cvsSplitBy);
                IDs.add(temp[0]);

                // In case need to display IDs while reading them...
                //System.out.println("Script ID = " + line);
            }
            // In case need to confirm that array was populated...
            //System.out.println("Total Scripts = " + IDs.size());

            //Populate global variable for total job IDs in the recommendation scope
            iTotalJobIDs = IDs.size();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return IDs;
    }



    /*
    Function to read CSV file with single column listing script IDs.

    Sample:   Job Library
       Script ID    Job Name                        Application     Location    Auto Tool   etc.
       11           A_ALM_UFT_Script_11             A               ALM         UFT
       13           A_ALM_Worksoft_Script_13        A               ALM         Worksoft
       15           A_GitHub_Selenium_Script_15     A               GitHub      Selenium

    Author: Fernanda Menks - Mar 2, 2017
 */
    public List<JobContainer> ReadJobLibrary(String ApplicationFolder){
        String csvFile = "./CSVs/"+ ApplicationFolder +"/Job_Library.csv";
        BufferedReader br = null;
        String line = "\n";
        String cvsSplitBy = ",";
        JobContainer tempJob;
        int temp_Script_ID;
        String temp_Job_Name;
        String temp_Application;
        String temp_Location;
        String temp_Auto_Tool;
        String temp_ALM_Domain;
        String temp_ALM_Project;
        String temp_ALM_Execution_Path;
        String temp_GitHub_Feature;
        String temp_GitHub_Repository_URL;

        try {
            br = new BufferedReader(new FileReader(csvFile));
            // Ignore header line from CSV file
            br.readLine();

            // Add all script IDs into array 'ScriptIDs'
            while ((line = br.readLine()) != null) {

                String [] temp = line.split(cvsSplitBy);
                temp_Script_ID = Integer.parseInt(temp[0]);
                temp_Job_Name = temp[1];
                temp_Application = temp[2];
                temp_Location = temp[3];
                temp_Auto_Tool = temp[4];
                temp_ALM_Domain = temp[5];
                temp_ALM_Project = temp[6];
                temp_ALM_Execution_Path = temp[7];
                if (temp.length > 8){
                    temp_GitHub_Feature = temp[8];
                    temp_GitHub_Repository_URL = temp[9];
                }else{
                    temp_GitHub_Feature = "";
                    temp_GitHub_Repository_URL = "";
                }
                tempJob = new JobContainer(temp_Script_ID, temp_Job_Name, temp_Application, temp_Location, temp_Auto_Tool
                             , temp_ALM_Domain, temp_ALM_Project, temp_ALM_Execution_Path, temp_GitHub_Feature
                            , temp_GitHub_Repository_URL);
                JobLibrary.add(tempJob);
            }//end of loop

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return JobLibrary;
    }



    /*
        Function to return the Job object from JobLibrary list based on the script ID
        Author: Fernanda Menks, March 2, 2017
     */
    /*public Job getJob(int pScript_ID){
        Job tempJob = new Job();
        for(int i=0; i<iTotalJobIDs; i++){
            tempJob = JobLibrary.get(i);
            if (tempJob.Script_ID = pScript_ID){
               break;
            }
        }
        return tempJob;
    }
*/

    /*
        Function to read CSV file from new auto scope and merge it into Job Library
        Sample:

            ****BEFORE****
            * New Auto Scope CSV:
               Script ID    Job Name                        Application     Location    Auto Tool   etc.
               12                                           A               ALM         LeanFT
               14                                           A               GitHub      Selenium

            * Job Library CSV:
               Script ID    Job Name                        Application     Location    Auto Tool   etc.
               11           A_ALM_UFT_Script_11             A               ALM         UFT
               13           A_ALM_Worksoft_Script_13        A               ALM         Worksoft
               15           A_GitHub_Selenium_Script_15     A               GitHub      Selenium




            ****AFTER****
            * New Auto Scope CSV: <empty>
               Script ID    Job Name                        Application     Location    Auto Tool   etc.

            * Job Library CSV:
               Script ID    Job Name                        Application     Location    Auto Tool   etc.
               11           A_ALM_UFT_Script_11             A               ALM         UFT
               12                                           A               ALM         LeanFT
               13           A_ALM_Worksoft_Script_13        A               ALM         Worksoft
               14                                           A               GitHub      Selenium
               15           A_GitHub_Selenium_Script_15     A               GitHub      Selenium

        Author: Fernanda Menks - Mar 1, 2017
     */
    public List<String> Merge_New_Auto_Scope_into_Library() {
        List<Path> paths = Arrays.asList(Paths.get("./CSVs/Application_A/Job_Library.csv"), Paths.get("./CSVs/Application_A/New_Auto_Scope.csv"));
        List<String> mergedLines = null;

        try {
            // Merge
            mergedLines = getMergedLines(paths);
            Path target = Paths.get("./CSVs/Application_A/Job_Library.csv");
            Files.write(target, mergedLines, Charset.forName("UTF-8"));

            // Clean new auto scope file
            FileWriter writer = new FileWriter("./CSVs/Application_A/New_Auto_Scope.csv", false);
            writer.write(""); // input empty data
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return mergedLines;
    }

    private static List<String> getMergedLines(List<Path> paths) throws IOException {
        List<String> mergedLines = null;
        try {
            mergedLines = new ArrayList<>();
            for (Path p : paths) {
                List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
                if (!lines.isEmpty()) {
                    if (mergedLines.isEmpty()) {
                        mergedLines.add(lines.get(0)); //add header only once
                    }
                    mergedLines.addAll(lines.subList(1, lines.size()));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return mergedLines;
    }



    public void Export_Execution_Results() throws IOException {
        String csvFile = "./CSVs/Execution_Result.csv";
        FileWriter writer = new FileWriter(csvFile);
        String ID = "";
        String Exec_Status = "";

        //Include header
        writeLine(writer, Arrays.asList("Script ID", "Execution Status"));

        //Include job ID + status
        for(int i=0; i<iTotalJobIDs; i++){
            ID = IDs.get(i);
            Exec_Status = "Passed";      //<<<<<<<<<<<<<<<<<<<<<<<<<<<--------------- Call method to parse json file
            writeLine(writer, Arrays.asList(ID, Exec_Status));
        }

        writer.flush();
        writer.close();
    }


    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }
    public static void writeLine(Writer w, List<String> values) throws IOException {

        boolean first = true;
        char separators = ',';
        char customQuote = ' ';


        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());
    }


    /*
    Method to
 */
    public static void Create_ALM_Job(JobContainer tempJob) throws IOException {
        if (tempJob.Job_Name.isEmpty()){
            tempJob.Job_Name = tempJob.Application + "_" + tempJob.Location + "_" + tempJob.Auto_Tool + "_Script_" + tempJob.Script_ID;
        }

        String csvFile = "./build/"+tempJob.Job_Name+".xml";
        FileWriter writer = new FileWriter(csvFile);

        writer.write("<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<project>\n" +
                "  <actions/>\n" +
                "  <description>"+"Pre-set job item for ALM script ID "+tempJob.Script_ID+" | Auto tool = "+tempJob.Auto_Tool+"</description>\n" +
                "  <keepDependencies>false</keepDependencies>\n"+
                "<properties/>\n" +
                "  <scm class=\"hudson.scm.NullSCM\"/>\n" +
                "  <canRoam>true</canRoam>\n" +
                "  <disabled>false</disabled>\n" +
                "  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>\n" +
                "  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>\n" +
                "  <triggers/>\n" +
                "  <concurrentBuild>false</concurrentBuild>\n" +
                "  <builders>\n" +
                "    <com.hp.application.automation.tools.run.RunFromAlmBuilder plugin=\"hp-application-automation-tools-plugin@5.1\">\n" +
                "      <runFromAlmModel>\n" +
                "        <almServerName>"+ALM_Server_Name+"</almServerName>\n" +
                "        <almUserName>"+ALM_Username+"</almUserName>\n" +
                "        <almPassword>"+ALM_Password+"</almPassword>\n" +
                "        <almDomain>"+tempJob.ALM_Domain+"</almDomain>\n" +
                "        <almProject>"+tempJob.ALM_Project+"</almProject>\n" +
                "        <almTestSets>"+tempJob.ALM_Execution_Path+"</almTestSets>\n" +
                "        <almTimeout></almTimeout>\n" +
                "        <almRunMode>RUN_LOCAL</almRunMode>\n" +
                "        <almRunHost></almRunHost>\n" +
                "      </runFromAlmModel>\n" +
                "      <ResultFilename>ApiResults.xml</ResultFilename>\n" +
                "      <ParamFileName>ApiRun.txt</ParamFileName>\n" +
                "    </com.hp.application.automation.tools.run.RunFromAlmBuilder>\n" +
                "  </builders>\n" +
                "  <publishers>\n" +
                "    <com.hp.application.automation.tools.results.RunResultRecorder plugin=\"hp-application-automation-tools-plugin@5.1\">\n" +
                "      <__resultsPublisherModel>\n" +
                "        <archiveTestResultsMode>ALWAYS_ARCHIVE_TEST_REPORT</archiveTestResultsMode>\n" +
                "      </__resultsPublisherModel>\n" +
                "    </com.hp.application.automation.tools.results.RunResultRecorder>\n" +
                "  </publishers>\n" +
                "  <buildWrappers>\n" +
                "    <hudson.plugins.build__timeout.BuildTimeoutWrapper plugin=\"build-timeout@1.18\">\n" +
                "      <strategy class=\"hudson.plugins.build_timeout.impl.LikelyStuckTimeOutStrategy\"/>\n" +
                "      <operationList>\n" +
                "        <hudson.plugins.build__timeout.operations.AbortOperation/>\n" +
                "      </operationList>\n" +
                "    </hudson.plugins.build__timeout.BuildTimeoutWrapper>\n" +
                "  </buildWrappers>\n" +
                "</project>");

        writer.flush();
        writer.close();

    }




    /*
        Main function to list all CSV files read in console output.
        This method isn't used in the real job creation. This is just for debug purposes.

        Author: Fernanda Menks - Feb 20, 2017
     */
    public static void main(String[] args) throws IOException {
        List<String> tempCSVcontent = new ArrayList<String>();
        JobDispatcherClass tempCSVfile = new JobDispatcherClass();
        JobContainer tempJob;

        //1. List input file with recommended job IDs
        tempCSVfile.ReadScriptIDs();
        System.out.println("Total Scripts in recommendation list = " + iTotalJobIDs);
        for(int i=0; i<iTotalJobIDs; i++){
            System.out.println(i+1 + ") "+ IDs.get(i));
        }


        //2. Merge new auto scope into pre-set job library
        tempCSVcontent = tempCSVfile.Merge_New_Auto_Scope_into_Library();
        System.out.println("\n\nTotal lines in Job Library CSV file = " + tempCSVcontent.size());
        for(String line : tempCSVcontent){
            System.out.println(line);
        }

        tempCSVfile.ReadJobLibrary("Application_A");
        System.out.println("\n\nTotal jobs objects in library = " + JobLibrary.size());
        //tempCSVfile.getJob(13);

        //3. Generate results CSV file
        tempCSVfile.Export_Execution_Results();

        //4. Confirm that
        tempJob = JobLibrary.get(0);
        Create_ALM_Job(tempJob);

    }
}

