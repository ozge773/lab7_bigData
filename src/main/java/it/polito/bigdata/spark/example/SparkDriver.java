package it.polito.bigdata.spark.example;

import scala.Tuple2;

import org.apache.spark.api.java.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;

public class SparkDriver {

	public static void main(String[] args) {

		// The following two lines are used to switch off some verbose log messages
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);

		String inputPath;
		String inputPath2;
		Double threshold;
		String outputFolder;

		inputPath = args[0];
		//inputPath2 = args[1];
		//threshold = Double.parseDouble(args[2]);
		outputFolder = args[1];//3

		// Create a configuration object and set the name of the application
		//SparkConf conf = new SparkConf().setAppName("Spark Lab #7");

		// Use the following command to create the SparkConf object if you want to run
		// your application inside Eclipse.
	    SparkConf conf=new SparkConf().setAppName("Spark Lab #7").setMaster("local");

		// Create a Spark Context object
		JavaSparkContext sc = new JavaSparkContext(conf);

		// print the application ID
		System.out.println("******************************");
		System.out.println("ApplicationId: "+JavaSparkContext.toSparkContext(sc).applicationId());
		System.out.println("******************************");

		JavaRDD<String> inputData = sc.textFile(inputPath);

        // Parse and Transform the Data
		// Parse and Transform the Data
        JavaPairRDD<String, Integer> dayTimeslotCriticality = inputData
            .flatMapToPair(line -> {
                String[] parts = line.split("\t"); // Splitting based on tab
                if (parts.length == 4) {
                    String timestamp = parts[1];
                    int usedSeats = Integer.parseInt(parts[2]);
                    int availableSeats = Integer.parseInt(parts[3]);

                    if (usedSeats == 0 && availableSeats == 0) {
                        return java.util.Collections.<Tuple2<String, Integer>>emptyList().iterator();
                    } else {
                        String date = timestamp.split(" ")[0];
                        int hour = Integer.parseInt(timestamp.split(" ")[1].split(":")[0]);
                        String dayOfWeek = DateTool.DayOfTheWeek(date);
                        String dayTimeslotKey = dayOfWeek + "-" + hour;
                        int isCritical = availableSeats == 0 ? 1 : 0;
                        return java.util.Collections.singletonList(new Tuple2<>(parts[0]+dayTimeslotKey, isCritical)).iterator();
                    }
                } else {
                    return java.util.Collections.<Tuple2<String, Integer>>emptyList().iterator();
                }
            });

        JavaPairRDD<String, Integer> criticalCounts = dayTimeslotCriticality
            .reduceByKey((a, b) -> a + b);


		criticalCounts.saveAsTextFile(outputFolder);

		

	

		




		// TODO



		// Store in resultKML one String, representing a KML marker, for each station 
		// with a critical timeslot 
		// JavaRDD<String> resultKML = .....
		  
		// Invoke coalesce(1) to store all data inside one single partition/i.e., in one single output part file
		// resultKML.coalesce(1).saveAsTextFile(outputFolder); 

		// Close the Spark context
		sc.close();
	}
}
