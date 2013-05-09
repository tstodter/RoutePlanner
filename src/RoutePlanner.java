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
import java.util.Comparator;


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

		Map<Edge, Edge> priors = new HashMap<Edge, Edge>();
		Set<String> visited = new HashSet<String>();
		Queue<Edge> Q = new PriorityQueue<Edge>(64, 
				new Comparator<Edge>() {
					@Override
					public int compare(Edge e1, Edge e2) {
						return e1.second.time.compareTo(e2.second.time);
					}			
		});

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
		Edge start = new Edge( new StopInTime(startStop, depTime), 
							   new StopInTime(startStop, depTime) );

		SortedSet<Edge> destinations = transit.getNext(start);

		System.out.println(destinations.first().first.stop.name + " " + destinations.first().first.time +
							" to " + destinations.first().second.stop.name + " " + destinations.first().second.time);

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

		//Q.add(destinations.first());
		Q.add(start);
		//visited.add(destinations.first().second.stop.id);

		Edge currentEdge = null;
		boolean found = false;

		boolean first = true;

		while (Q.isEmpty() == false) {

			currentEdge = Q.remove();

			System.out.println(currentEdge.first.stop.name + " * " + currentEdge.first.time + "-" +
                                           currentEdge.second.stop.name + "*" + currentEdge.second.time);

			//visited.add(currentEdge.second.stop);

			if (currentEdge.second.stop.id.equals(endId)) {
				System.out.println("Id found");
				found = true;
				break;
			}

			SortedSet<Edge> nextEdges = transit.getNext(currentEdge);
                        if (nextEdges == null) continue;

			for (Edge e : nextEdges) {
				if (priors.containsKey(e) == false)
					priors.put(e, currentEdge);
				if (visited.contains(e.second.stop.id) == false) {
					Q.add(e);
					visited.add(e.second.stop.id);
					if (e.second.stop.name.equals("FORT TOTTEN METRO STATION"))
						System.out.println("*****" + "FORT TOTTEN METRO STATION " + e.second.time);
				}
			}

			if (first) {
				System.out.println(nextEdges.first().first.stop.name + " " + nextEdges.first().first.time);
				first = false;
			}

		}

		if (found) {

			System.out.println("found! last stop is " + currentEdge.second.stop.name + " at " + currentEdge.second.time);

			while(currentEdge!= start){
				System.out.println(currentEdge.second.stop.name + " " + currentEdge.second.time);
				currentEdge = priors.get(currentEdge);
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
				String depTime = prevLineTokens[2];
				String arrTime = nextLineTokens[2];

				Edge newEdge = new Edge( new StopInTime(fromStop, depTime), new StopInTime(toStop, arrTime));

				transit.put(fromStop, newEdge);

				prevLineTokens = nextLineTokens;

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

		HashMap<Stop, TreeSet<Edge>> edges;

		public TransitSystem() {
			edges = new HashMap< Stop, TreeSet<Edge>>();
		}

		public SortedSet<Edge> getNext(Edge prior) {

			TreeSet<Edge> set = edges.get(prior.second.stop);
                        
                        for (Edge t: set) 
                             if (t.first.time.compareTo(prior.second.time) >= 0)
                                 return set.tailSet(t);

			return null;
		}

		public void put(Stop from, Edge edge) {
			if ( edges.containsKey(from) == false ) {
				edges.put(from, new TreeSet<Edge>());
			}
			edges.get(from).add(edge);	
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

		public StopInTime(Stop stop, String time) {
			this.stop = stop;
			this.time = time;
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

	class Edge implements Comparable<Edge> {
		StopInTime first;
		StopInTime second;

		public Edge(StopInTime first, StopInTime second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public int compareTo(Edge later) {
			try {
				int result = this.first.time.compareTo(later.first.time);
			} catch(Exception e) {
				System.out.println(later.first.stop == null? "null" : later.first.stop);
				System.out.println(later.first.stop.name == null? "null" : later.first.stop.name);
				System.out.println(later.first.time == null? "null" : later.first.time);
				System.out.println(later.second.stop.name == null? "null" : later.second.stop.name);
				System.out.println(later.second.time == null? "null" : later.second.time);
			}
			if  (this.first.time.compareTo(later.first.time) != 0) 
                             return this.first.time.compareTo(later.first.time);
                        if (this.second.stop.id.compareTo(later.second.stop.id) != 0)
                             return this.second.stop.id.compareTo(later.second.stop.id);
                        return this.second.time.compareTo(later.second.time);
		}
	}



}
