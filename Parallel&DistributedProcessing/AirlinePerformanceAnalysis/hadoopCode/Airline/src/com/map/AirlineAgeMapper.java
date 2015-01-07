package com.map;

import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class AirlineAgeMapper extends Mapper<Object, Text, Text, Text> {

	private static final String YEAR = "Year";
	private static final String MONTH = "Month";
	private static final String DAY_OF_MONTH = "DayofMonth";
	private static final String DAY_OF_WEEK = "DayOfWeek";
	private static final String DEP_TIME = "DepTime";
	private static final String CRS_DEP_TIME = "CRSDepTime";
	private static final String ARR_TIME = "ArrTime";
	private static final String CRS_ARR_TIME = "CRSArrTime";
	private static final String UNIQUE_CARRIER = "UniqueCarrier";
	private static final String FLIGHT_NUM = "FlightNum";
	private static final String TAIL_NUM = "TailNum";
	private static final String ACTUAL_ELAPSED_TIME = "ActualElapsedTime";
	private static final String CRS_ELAPSED_TIME = "CRSElapsedTime";
	private static final String AIR_TIME = "AirTime";
	private static final String ARR_DELAY = "ArrDelay";
	private static final String DEP_DELAY = "DepDelay";
	private static final String ORIGIN = "Origin";
	private static final String DEST = "Dest";
	private static final String DISTANCE = "Distance";
	private static final String TAXI_IN = "TaxiIn";
	private static final String TAXI_OUT = "TaxiOut";
	private static final String CANCELLED = "Cancelled";
	private static final String CANCELLATION_CODE = "CancellationCode";
	private static final String DIVERTED = "Diverted";
	private static final String CARRIER_DELAY = "CarrierDelay";
	private static final String WEATHER_DELAY = "WeatherDelay";
	private static final String NAS_DELAY = "NASDelay";
	private static final String SECURITY_DELAY = "SecurityDelay";
	private static final String LATE_AIRCRAFT_DELAY = "LateAircraftDelay";
	private static final String SPACE = " ";

	String[] cols = null;
	static HashMap<String, Integer> headerMap = new HashMap<String, Integer>(45);
	Text emitKey = new Text();
	Text emitVal = new Text();
	StringBuilder sb = new StringBuilder(1024);

	@Override
	protected void setup(Mapper<Object, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		super.setup(context);
		initializeHeaderMap();
	}

	public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException {
		cols = value.toString().split(",");
		if (!cols[0].equals("Year")) {
			// Check if arrival delay was there
			if (cols[headerMap.get(ARR_DELAY)].equals("NA")
					|| Integer.parseInt(cols[headerMap.get(ARR_DELAY)]) > 0) {
				// Key is (Year UniqueCarrier FlightNum TailNum)
				// Val is (ArrDelay CarrierDelay WeatherDelay
				// NASDelay SecurityDelay LateAircraftDelay Cancelled
				// CancellationCode Diverted)
				emitKey.set(populateKeys());
				emitVal.set(populateValues());
				context.write(emitKey, emitVal);
			}
		}
	}

	private String populateKeys() {
		sb.setLength(0);
		sb.append(cols[headerMap.get(YEAR)] + SPACE);
		sb.append(cols[headerMap.get(UNIQUE_CARRIER)] + SPACE);
		sb.append(cols[headerMap.get(FLIGHT_NUM)] + SPACE);
		sb.append(cols[headerMap.get(TAIL_NUM)] + SPACE);
		return sb.toString();
	}

	private String populateValues() {
		sb.setLength(0);
		sb.append(cols[headerMap.get(ARR_DELAY)] + SPACE);
		sb.append(cols[headerMap.get(CARRIER_DELAY)] + SPACE);
		sb.append(cols[headerMap.get(WEATHER_DELAY)] + SPACE);
		sb.append(cols[headerMap.get(NAS_DELAY)] + SPACE);
		sb.append(cols[headerMap.get(SECURITY_DELAY)] + SPACE);
		sb.append(cols[headerMap.get(LATE_AIRCRAFT_DELAY)] + SPACE);
		sb.append(cols[headerMap.get(CANCELLED)] + SPACE);
		sb.append(cols[headerMap.get(CANCELLATION_CODE)] + SPACE);
		sb.append(cols[headerMap.get(DIVERTED)] + SPACE);
		return sb.toString();
	}

	@Override
	protected void cleanup(Mapper<Object, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		super.cleanup(context);
		headerMap.clear();
		sb.setLength(0);
	}

	private static void initializeHeaderMap() {
		int i = 0;
		headerMap.put(YEAR, i++);
		headerMap.put(MONTH, i++);
		headerMap.put(DAY_OF_MONTH, i++);
		headerMap.put(DAY_OF_WEEK, i++);
		headerMap.put(DEP_TIME, i++);
		headerMap.put(CRS_DEP_TIME, i++);
		headerMap.put(ARR_TIME, i++);
		headerMap.put(CRS_ARR_TIME, i++);
		headerMap.put(UNIQUE_CARRIER, i++);
		headerMap.put(FLIGHT_NUM, i++);
		headerMap.put(TAIL_NUM, i++);
		headerMap.put(ACTUAL_ELAPSED_TIME, i++);
		headerMap.put(CRS_ELAPSED_TIME, i++);
		headerMap.put(AIR_TIME, i++);
		headerMap.put(ARR_DELAY, i++);
		headerMap.put(DEP_DELAY, i++);
		headerMap.put(ORIGIN, i++);
		headerMap.put(DEST, i++);
		headerMap.put(DISTANCE, i++);
		headerMap.put(TAXI_IN, i++);
		headerMap.put(TAXI_OUT, i++);
		headerMap.put(CANCELLED, i++);
		headerMap.put(CANCELLATION_CODE, i++);
		headerMap.put(DIVERTED, i++);
		headerMap.put(CARRIER_DELAY, i++);
		headerMap.put(WEATHER_DELAY, i++);
		headerMap.put(NAS_DELAY, i++);
		headerMap.put(SECURITY_DELAY, i++);
		headerMap.put(LATE_AIRCRAFT_DELAY, i++);
	}

}
