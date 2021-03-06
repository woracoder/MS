package com.reduce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.pojos.AirportDetails;
import com.pojos.CarrierDetails;
import com.pojos.PlaneDetails;

public class AirlineAgeReducer extends Reducer<Text, Text, Text, Text> {

	String[] keyCols = null;
	String[] valCols = null;
	String cancelReason = null;
	Text finalKey = new Text();
	Text finalVal = new Text();
	StringBuilder sbk = new StringBuilder(1024);
	StringBuilder sbv = new StringBuilder(1024);
	HashMap<String, Integer> positionMap = new HashMap<String, Integer>();
	HashMap<String, CarrierDetails> carriersMap = new HashMap<String, CarrierDetails>();
	HashMap<String, AirportDetails> airportsMap = new HashMap<String, AirportDetails>();
	HashMap<String, PlaneDetails> planeDetailsMap = new HashMap<String, PlaneDetails>();
	HashMap<Integer, String> monthMap = new HashMap<Integer, String>();

	private static final String ARR_DELAY = "ArrDelay";
	private static final String CARRIER_DELAY = "CarrierDelay";
	private static final String WEATHER_DELAY = "WeatherDelay";
	private static final String NAS_DELAY = "NASDelay";
	private static final String SECURITY_DELAY = "SecurityDelay";
	private static final String LATE_AIRCRAFT_DELAY = "LateAircraftDelay";
	private static final String CANCELLED = "Cancelled";
	private static final String CANCELLATION_CODE = "CancellationCode";
	private static final String DIVERTED = "Diverted";
	private static final String SPACE = " ";

	private static final String JANUARY = "Jan";
	private static final String FEBRUARY = "Feb";
	private static final String MARCH = "Mar";
	private static final String APRIL = "Apr";
	private static final String MAY = "May";
	private static final String JUNE = "Jun";
	private static final String JULY = "Jul";
	private static final String AUGUST = "Aug";
	private static final String SEPTEMBER = "Sep";
	private static final String OCTOBER = "Oct";
	private static final String NOVEMBER = "Nov";
	private static final String DECEMBER = "Dec";

	private void populatePositionAndMonthMap() {
		int i = 0;
		positionMap.put(ARR_DELAY, i++);
		positionMap.put(CARRIER_DELAY, i++);
		positionMap.put(WEATHER_DELAY, i++);
		positionMap.put(NAS_DELAY, i++);
		positionMap.put(SECURITY_DELAY, i++);
		positionMap.put(LATE_AIRCRAFT_DELAY, i++);
		positionMap.put(CANCELLED, i++);
		positionMap.put(CANCELLATION_CODE, i++);
		positionMap.put(DIVERTED, i++);

		i = 1;
		monthMap.put(i++, JANUARY);
		monthMap.put(i++, FEBRUARY);
		monthMap.put(i++, MARCH);
		monthMap.put(i++, APRIL);
		monthMap.put(i++, MAY);
		monthMap.put(i++, JUNE);
		monthMap.put(i++, JULY);
		monthMap.put(i++, AUGUST);
		monthMap.put(i++, SEPTEMBER);
		monthMap.put(i++, OCTOBER);
		monthMap.put(i++, NOVEMBER);
		monthMap.put(i++, DECEMBER);

	}

	@Override
	protected void setup(Reducer<Text, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		super.setup(context);
		populatePositionAndMonthMap();
		Configuration conf = context.getConfiguration();
		FileSystem fs = FileSystem.get(conf);
		try {
			populateCarrierAirportPlaneDetails(fs);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void populateCarrierAirportPlaneDetails(FileSystem fs)
			throws IOException, ParseException {
		String tmp = null;
		String[] tmps = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(
				fs.open(new Path("/data/carriers.csv"))));
		br.readLine();
		while ((tmp = br.readLine()) != null) {
			tmps = tmp.split(",");
			carriersMap.put(tmps[0], new CarrierDetails(tmps[0], tmps[1]));
		}
		br.close();

		BufferedReader brd = new BufferedReader(new InputStreamReader(
				fs.open(new Path("/data/airports.csv"))));
		brd.readLine();
		while ((tmp = brd.readLine()) != null) {
			tmps = tmp.split(",");
			airportsMap.put(tmps[0], new AirportDetails(tmps[0], tmps[1],
					tmps[2], tmps[3], tmps[4], Double.parseDouble(tmps[5]),
					Double.parseDouble(tmps[6])));
		}
		brd.close();

		SimpleDateFormat mdyFormat = new SimpleDateFormat("MM/dd/yyyy");
		BufferedReader burd = new BufferedReader(new InputStreamReader(
				fs.open(new Path("/data/plane-data.csv"))));
		burd.readLine();
		while ((tmp = burd.readLine()) != null) {
			tmps = tmp.split(",");
			if (tmps.length < 9) {
				continue;
			}
			planeDetailsMap.put(tmps[0], new PlaneDetails(tmps[0], tmps[1],
					tmps[2], mdyFormat.parse(tmps[3]), tmps[4], tmps[5],
					tmps[6], tmps[7], tmps[8]));
		}
		burd.close();
	}

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		sbk.setLength(0);
		keyCols = key.toString().split("\\s+");
		sbk.append(keyCols[0] + SPACE);
		sbk.append(monthMap.get(Integer.valueOf(keyCols[1])) + SPACE);
		sbk.append(carriersMap.get(keyCols[2]) + SPACE);
		sbk.append(airportsMap.get(keyCols[3]) + SPACE);
		sbk.append(airportsMap.get(keyCols[4]) + SPACE);
		finalVal.set(sbk.toString());

		sbv.setLength(0);

		int totalDelayTime = 0, countAllDelays = 0, countAllFlights = 0;

		for (Text val : values) {
			// valCols has (ArrDelay CarrierDelay WeatherDelay NASDelay
			// SecurityDelay LateAircraftDelay Cancelled CancellationCode
			// Diverted)
			countAllFlights++;
			valCols = val.toString().split("\\s+");

			if (!valCols[positionMap.get(ARR_DELAY)].equals("NA")) {
				 countAllDelays++;
				 totalDelayTime += Integer.parseInt(valCols[positionMap.get(CARRIER_DELAY)]);
			} 
		}
		
		

		sbv.append(countAllFlights + SPACE);
		sbv.append(countAllDelays + SPACE);
		sbv.append(totalDelayTime + SPACE);
		sbv.append((int)totalDelayTime/(1.0 * countAllDelays) + SPACE);
		
		finalVal.set(sbv.toString());
		context.write(finalKey, finalVal);
	}

	@Override
	protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		super.cleanup(context);
		positionMap.clear();
		monthMap.clear();
		carriersMap.clear();
		planeDetailsMap.clear();
		sbk.setLength(0);
		sbv.setLength(0);
	}

}
