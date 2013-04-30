import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


public class RoutePlanner {
	
	public final int BUFFER_SIZE = 1024 * 4;
	
	public static void main(String[] args) {
		RoutePlanner program = new RoutePlanner();
		program.run();
	}

	void run() {
		//Tools
		Scanner scan = new Scanner(System.in);
		
		//
		TransitSystem transit = new TransitSystem();
		Map<String, Trip> trips = new HashMap<String, Trip>();
		Map<String, Stop> stops = new HashMap<String, Stop>();
		Map<String, Route> routes = new HashMap<String, Route>();
		
		Queue<StopInTime> Q = new PriorityQueue<StopInTime>();
		Map<StopInTime, StopInTime> priors = new HashMap<StopInTime, StopInTime>();
		Set<StopInTime> visited = new HashSet<StopInTime>();
		
		//
		getTripData(trips);
		getStopData(stops);
		getRouteData(routes);
		getStopTimeData(transit,stops);
		
		System.out.println("Input now:");

		String startId = scan.next();
		String endId = scan.next();
		String depHour = scan.next();
		String depMin = scan.next();
		
		int depHourInt = Integer.parseInt(depHour);
		int depMinInt = Integer.parseInt(depMin);
		
		//Put together complete departure time
		depHour = (depHourInt < 10? "0" + depHourInt : depHour);
		depMin =  (depMinInt < 10? "0" + depMinInt : depMin);
		String depTime = depHour + ":" + depMin + ":00";
		
		//Get stops
		Stop startStop = stops.get(startId);
		Stop endStop = stops.get(endId);
		
		//Get earliest trips
		StopInTime start = new StopInTime(startStop, depTime, "0");
						
		SortedSet<StopInTime> destinations = transit.getNext(start);
		
		System.out.println(destinations.first().stop.name + " " + destinations.first().time);
		
		/*//////////////////////////////////////
		System.out.println(start.stop.name + " " + start.time + " " + start.stop.id + " ");
		for (StopInTime s : destinations) {
			System.out.println("*** " + s.stop.name + " " + s.time + " " + s.stop.id + " " + s.stopSequence);
		}
		System.out.println();
		Scanner sc = new Scanner(System.in);
		String temp = sc.next();
		
		*///////////////////////////////////////
		
		//Initialize queue
		for (StopInTime s : destinations) 
			priors.put(s, start);
		
		Q.addAll(destinations);
		
		StopInTime currentStop = null;
		boolean found = false;

		boolean first = true;
		
		while (Q.isEmpty() == false) {
						
			currentStop = Q.remove();
			
			visited.add(currentStop);
						
			if (currentStop.stop.id.equals(endId)) {
				System.out.println("Id found");
				found = true;
				break;
			}
			
			if (currentStop.stop.equals(endStop)) {
				System.out.println("found");
				//Destination found
				found = true;
				break;
			}
			
			SortedSet<StopInTime> nextStops = transit.getNext(currentStop);
			
			for (StopInTime s : nextStops) {
				if (priors.containsKey(s) == false)
					priors.put(s, currentStop);
				if (visited.contains(s) == false)
					Q.add(s);
			}
			
			if (first) {
				System.out.println(nextStops.first().stop.name + " " + nextStops.first().time);
				first = false;
			}
				
		}
		
		if (found) {
			
			System.out.println("found! last stop is " + currentStop.stop.name + " at " + currentStop.time);
			
			while(currentStop.stop != startStop){
				System.out.println(currentStop.stop.name + " " + currentStop.time);
				currentStop = priors.get(currentStop);
			}
		}	
		else
			System.out.println("No route found");
	}
	
	/*
	 * 
	 * 
	 */
	
	void getStopTimeData( TransitSystem transit , Map<String, Stop> stops) {
	//void getStopTimeData( HashMap< Hop, TreeSet<Hop>> transitSystem , Map<String, Stop> stops) {
	
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("./input/stop_times.txt"), BUFFER_SIZE);
			
			String[] prevLineTokens = null;
			
			String line = reader.readLine();		//Throw away initial line
			int lineNum = 0;
			while ((line = reader.readLine()) != null) {
				
				lineNum++;
				
				String[] nextLineTokens = line.split(",");
				String stopSequence = null;
				try{
					stopSequence = nextLineTokens[4];
				} catch( Exception e) {
					System.out.println(lineNum);
				}
				
				if (stopSequence.equals("1")) {				//Continue if this is the beginning stop in
					prevLineTokens = nextLineTokens;		//this new trip
					continue;
				}
				
				Stop fromStop = stops.get(prevLineTokens[3]);
				Stop toStop = stops.get(nextLineTokens[3]);
				String arrTime = nextLineTokens[2];
								
				transit.put(fromStop, new StopInTime(toStop, arrTime, stopSequence));
				
				prevLineTokens = nextLineTokens;
				
				//if ( fromStop.name.equals("FEDERAL CENTER METRO STATION") &&  )
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {	
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	void getStopData(Map<String, Stop> stops) {
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("./input/stops.txt"), BUFFER_SIZE);
			
			String line = reader.readLine();		//Throw away initial line
			while ((line = reader.readLine()) != null) {
				
				String[] tokens = line.split(",");
				
				String stopId = tokens[0],
					   name = tokens[1],
					   desc = tokens[2],
					   lat = tokens[3],
					   lon = tokens[4],
					   zoneId = tokens[5];
				
				Stop newStop = new Stop(stopId, name);
				//Stop newStop = new Stop(stopId, name, desc, lat, lon, zoneId);
				stops.put(stopId, newStop);		
			
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {	
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	void getTripData(Map<String, Trip> trips) {
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("./input/trips.txt"), BUFFER_SIZE);
			
			String line = reader.readLine();			//Throw away initial line
			while ((line = reader.readLine()) != null) {
				
				String[] tokens = line.split(",");
				
				String routeId = tokens[0],
					   serviceId = tokens[1],
					   tripId = tokens[2],
					   headSign = tokens[3],
					   directionId = tokens[4],
					   blockId = tokens[5],
					   shapeId = tokens[6];
				
				trips.put(tripId, new Trip(routeId, serviceId, tripId, headSign, directionId, blockId, shapeId));
			
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {	
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	void getRouteData(Map<String, Route> routes) {
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("./input/routes.txt"), BUFFER_SIZE);
			
			String line = reader.readLine();		//Throw away initial line
			while ((line = reader.readLine()) != null) {
				
				String[] tokens = line.split(",");
			
				String routeId = tokens[0],
					   agencyId = tokens[1],
					   shortName = tokens[2],
					   longName = tokens[3],
					   type = tokens[4],
					   url = tokens[5];
				//Consider the case where input line ends as "...,url," where tokens[6] does not exist
				String color;
				if (tokens.length == 7)
					color = tokens[6];
				else color = null;
				
				routes.put(routeId, new Route(routeId, agencyId, shortName, longName, type, url, color));
			
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {	
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class TransitSystem {
		
		HashMap<Stop, TreeSet<StopInTime>> stopToLaterStops;
		
		public TransitSystem() {
			stopToLaterStops = new HashMap< Stop, TreeSet<StopInTime>>();
		}
		
		public SortedSet<StopInTime> getNext(StopInTime stopInTime) {
			
			TreeSet<StopInTime> set = stopToLaterStops.get(stopInTime.stop);
						
			//System.out.println(set);
			
			return set.tailSet(stopInTime, false);
		}
		
		public void put(Stop from, StopInTime to) {
			if ( stopToLaterStops.containsKey(from) == false ) {
				stopToLaterStops.put(from, new TreeSet<StopInTime>());
			}
			stopToLaterStops.get(from).add(to);	
		}
		
	}
	
	class Stop {
		
		String id;
		String name; 
		String desc;
		String lat;
		String lon;
		String zoneId;
		
		public Stop(String id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public Stop (String id, String name, String desc, String lat, String lon, String zoneId) {
			this.id = id;
			this.name = name;
			this.desc = desc;
			this.lat = lat;
			this.lon = lon;
			this.zoneId = zoneId;
		}
		
		@Override
		public boolean equals(Object other) {
			Stop otherStop = (Stop) other;
			return this.id == otherStop.id;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			
			result = prime * result + id.hashCode();
			
			return result;
		}

	}
	
	class Trip {
		
		String routeId;
		String serviceId;
		String tripId;
		String headSign;
		String directionId;
		String blockId;
		String shapeId;
		
		public Trip (String routeId, String serviceId, String tripId, String headSign, 
					 String directionId, String blockId, String shapeId) {
			
			this.routeId = routeId;
			this.serviceId = serviceId;
			this.tripId = tripId;
			this.headSign = headSign;
			this.directionId = directionId;
			this.blockId = blockId;
			this.shapeId = shapeId;		
		}
	}
	
	class Route {
		
		String routeId;
		String agencyId;
		String shortName;
		String longName;
		String type; 
		String url; 
		String color;
		
		public Route (String routeId, String agencyId, String shortName, String longName, 
					  String type, String url, String color) {
			
			this.routeId = routeId;
			this.agencyId = agencyId;
			this.shortName = shortName;
			this.longName = longName;
			this.type = type;
			this.url = url;
			this.color = color;
		}
	}
	/*
	class StopHop implements Comparable {
		
		public Stop fromStop;
		public String departureTime;
		public Stop toStop;
		public String arrivalTime;
		
		public StopHop(Stop fromStop, String depTime, Stop toStop, String arrTime) {
			this.fromStop = fromStop;
			this.departureTime = depTime;
			this.toStop = toStop;
			this.arrivalTime = arrTime;
		}
		
		@Override
		public int compareTo(Object other) {
			StopHop otherHop = (StopHop) other;
			
			if (otherHop.departureTime > this.arrivalTime) return 1;
			if (otherHop.departureTime == this.arrivalTime) return 0;
			if (otherHop.departureTime < this.arrivalTime) return -1;	
		}
	}
	*/
	class StopInTime implements Comparable<StopInTime> {
		
		public Stop stop;
		public String time;
		public String stopSequence;
		
		public StopInTime(Stop stop, String time, String stopSequence) {
			this.stop = stop;
			this.time = time;
			this.stopSequence = stopSequence;
		}
		
		@Override
		public int compareTo(StopInTime other) {
			
			return this.time.compareTo(other.time);
		}
		
		@Override
		public boolean equals(Object other) {
			return this.stop == ((StopInTime) other).stop;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			
			result = prime * result + stop.hashCode();
			
			return result;
		}
	}
	
	class Edge {
		
	}
	
	 

}
