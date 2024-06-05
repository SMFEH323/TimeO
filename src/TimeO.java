import java.io.*;
import java.util.*;
import directedgraph.*;
import graph.*;

/**
 * @author Sayf Elhawary
 */
public class TimeO {

	private static AdjacencyListDirectedGraph timeoGraph =
	    new AdjacencyListDirectedGraph();
	private static String startPoint; // starting point name on the track
	private static AdjacencyListDirectedVertex startNode;// start vertex
	private static double timeLimit; // the time limit for completing the course
	private static double penalty; // the penalty (in points) per minute late
	private static double pace; // the pace of the runner
	private static long nodesVisited = 0;

	// contains the name of control points and their details
	private static HashMap<String,double[]> controlPointsDetails =
	    new HashMap<String,double[]>();
	// contains the name of control points and their vertices
	private static HashMap<String,AdjacencyListDirectedVertex> controlPoints =
	    new HashMap<String,AdjacencyListDirectedVertex>();

	private static Path best = new Path();

	public static void main ( String[] args ) throws IOException {
		BufferedReader readFile = new BufferedReader(new FileReader(args[1]));
		startPoint = "start";
		String[] timeDetails = readFile.readLine().split(" +");
		timeLimit = Double.parseDouble(timeDetails[1]);
		penalty = Double.parseDouble(timeDetails[2]);
//		System.out.println(timeLimit + " " + penalty);
//		System.out.println();
		int totalControls = Integer.parseInt(readFile.readLine().split(" +")[1]);
//		System.out.println(totalControls);
//		System.out.println();
		// load the control points into the hashmap
		for ( int i = 0 ; i < totalControls ; i++ ) {
			String[] currControlDetails = readFile.readLine().split(" +");
			double currPointValue = Double.parseDouble(currControlDetails[1]);
			double currOpenTime = Double.parseDouble(currControlDetails[2]);
			double currEndTime = Double.parseDouble(currControlDetails[3]);
			controlPointsDetails
			    .put(currControlDetails[0],
			         new double[] { currPointValue, currOpenTime, currEndTime });
//			System.out.println(currControlDetails[0] + " " + currControlDetails[1] + " " + currControlDetails[2] + " " + currControlDetails[3]);
//			System.out.println();
		}
		controlPointsDetails.put(startPoint,new double[] { 0, 0, 30 });

		readFile.close();

		HashSet<AdjacencyListDirectedVertex> controls = generateGraph(args[0]);
		pace = Double.parseDouble(args[2]) / 1000;
		// System.out.println(pace);

		Path initial = new Path();
		initial.addToPath(startNode,0,0,0);
		long t = System.currentTimeMillis();
		best = new Path();
		best = timeo(initial,startNode,controls,timeLimit);
		System.out.println("Total MilliSeconds: " + (System.currentTimeMillis() - t)
		    + "    Nodes Visited: " + nodesVisited);
		best.printPath();

	}

	public static Path timeo ( Path partialSolution, Vertex currControl,
	                           HashSet<AdjacencyListDirectedVertex> controlsLeft,
	                           double otlRemaining ) {

		if ( currControl == startNode
		    && partialSolution.getNumControlsVisited() > 1 ) {
			Path completeSolution = new Path();
			completeSolution.clonePath(partialSolution);
			// completeSolution.printPath();
			nodesVisited++;
			return completeSolution;
		} else {
			double total = 0;
			for ( AdjacencyListDirectedVertex v : controlsLeft ) {
				if ( controlPointsDetails
				    .get((String) v.getObject())[2] >= (partialSolution
				        .getTotaltime()) ) {
					total += controlPointsDetails.get((String) v.getObject())[0];
				}
			}

			if ( partialSolution.getTotalScore() + total > best.getTotalScore() ) {
				// for each incident vertex for the current vertex
				for ( Edge incidentEdge : timeoGraph.inIncidentEdges(currControl) ) {
					AdjacencyListDirectedVertex opposite =
					    (AdjacencyListDirectedVertex) timeoGraph.opposite(currControl,
					                                                      incidentEdge);
					if ( controlsLeft.contains(opposite) ) {
						String desiredControl = (String) opposite.getObject();
						double distance = (double) incidentEdge.getObject();
						double timeTaken = (pace * distance);
						controlsLeft.remove(opposite);
						double[] controlDetails = controlPointsDetails.get(desiredControl);
						if ( timeTaken > otlRemaining ) {
							if ( controlDetails[1] <= (partialSolution.getTotaltime()
							    + timeTaken)
							    && controlDetails[2] >= (partialSolution.getTotaltime()
							        + timeTaken) ) {
								double penaltyTime = timeTaken - otlRemaining;
								double penaltyGiven = (penalty * penaltyTime);
								double controlPoint = controlDetails[0];
								partialSolution
								    .addToPath(opposite,
								               (partialSolution.getTotaltime() + timeTaken),
								               controlPoint,penaltyGiven);
								Path result = timeo(partialSolution,opposite,controlsLeft,0);
								partialSolution.removeRecentPath(controlPoint,penaltyGiven);
								if ( result.getTotalScore() > best.getTotalScore() ) {
									best = result;
								}
							} else if ( controlDetails[1] > (partialSolution.getTotaltime()
							    + timeTaken) ) {
								    double timeWaited = controlDetails[1]
								        - (partialSolution.getTotaltime() + timeTaken);
								    double penaltyTime =
								        (timeTaken + timeWaited) - otlRemaining;
								    double penaltyGiven = (penalty * penaltyTime);
								    double controlPoint = controlDetails[0];
								    partialSolution.addToPath(opposite,
								                              (partialSolution.getTotaltime()
								                                  + timeTaken + timeWaited),
								                              controlPoint,penaltyGiven);
								    Path result =
								        timeo(partialSolution,opposite,controlsLeft,0);
								    partialSolution.removeRecentPath(controlPoint,penaltyGiven);
								    if ( result.getTotalScore() > best.getTotalScore() ) {
									    best = result;
								    }
							    }
						} else {
							if ( controlDetails[1] <= (partialSolution.getTotaltime()
							    + timeTaken)
							    && controlDetails[2] >= (partialSolution.getTotaltime()
							        + timeTaken) ) {
								double controlPoint = controlDetails[0];
								partialSolution
								    .addToPath(opposite,
								               (partialSolution.getTotaltime() + timeTaken),
								               controlPoint,0);
								Path result = timeo(partialSolution,opposite,controlsLeft,
								                    otlRemaining - timeTaken);
								partialSolution.removeRecentPath(controlPoint,0);
								if ( result.getTotalScore() > best.getTotalScore() ) {
									best = result;
								}
							} else if ( controlDetails[1] > (partialSolution.getTotaltime()
							    + timeTaken) ) {
								    double timeWaited = controlDetails[1]
								        - (partialSolution.getTotaltime() + timeTaken);
								    double controlPoint = controlDetails[0];
								    partialSolution.addToPath(opposite,
								                              (partialSolution.getTotaltime()
								                                  + timeTaken + timeWaited),
								                              controlPoint,0);
								    Path result = timeo(partialSolution,opposite,controlsLeft,
								                        otlRemaining - timeTaken);
								    partialSolution.removeRecentPath(controlPoint,0);
								    if ( result.getTotalScore() > best.getTotalScore() ) {
									    best = result;
								    }
							    }
						}
						controlsLeft.add(opposite);
					}
				}
			}

			return best;
		}

	}

	/**
	 * Creates a directed, weighted graph
	 * 
	 * @param fileName
	 *          containing graph data
	 * @throws IOException
	 */
	public static HashSet<AdjacencyListDirectedVertex> generateGraph ( String fileName )
	    throws IOException {
		HashSet<AdjacencyListDirectedVertex> controls =
		    new HashSet<AdjacencyListDirectedVertex>();
		startNode =
		    (AdjacencyListDirectedVertex) timeoGraph.insertVertex(startPoint);
		controls.add(startNode);
		BufferedReader readFile = new BufferedReader(new FileReader(fileName));
		String[] controlsDetails = readFile.readLine().split(" +");
		int totalControls = Integer.parseInt(controlsDetails[1]);
		for ( int i = 0 ; i < totalControls ; i++ ) {
			AdjacencyListDirectedVertex currVertex =
			    (AdjacencyListDirectedVertex) timeoGraph
			        .insertVertex(controlsDetails[i + 2]);
			controlPoints.put(controlsDetails[i + 2],currVertex);
			controls.add(currVertex);
			// System.out.println(controlsDetails[i + 2]);
		}
		for ( String lineRead = readFile.readLine() ; lineRead != null ; lineRead =
		    readFile.readLine() ) {
			String lineComponents[] = lineRead.split(" ");

			AdjacencyListDirectedVertex fVertex = null;
			if ( lineComponents[0].equals(startPoint) ) {
				fVertex = startNode;
			} else {
				fVertex = controlPoints.get(lineComponents[0]);
			}

			AdjacencyListDirectedVertex sVertex = null;
			if ( lineComponents[1].equals(startPoint) ) {
				sVertex = startNode;
			} else {
				sVertex = controlPoints.get(lineComponents[1]);
			}

			timeoGraph.insertDirectedEdge(fVertex,sVertex,
			                              Double.parseDouble(lineComponents[2]));
			timeoGraph.insertDirectedEdge(sVertex,fVertex,
			                              Double.parseDouble(lineComponents[3]));
			// System.out.println(fVertex.getObject() + " " + sVertex.getObject() + "
			// " + lineComponents[2] + " " + lineComponents[3]);

		}

		readFile.close();
		return controls;
	}

	public static class Path {
		private double totalTime;
		private double rawScore;
		private double totalPenalty;
		private List<Vertex> controlsVisited;
		private List<Double> timesVisited;
		private List<Double> pointsScored;

		public Path () {
			totalTime = 0;
			rawScore = 0;
			totalPenalty = 0;
			controlsVisited = new ArrayList<Vertex>();
			timesVisited = new ArrayList<Double>();
			pointsScored = new ArrayList<Double>();
		}

		public double getTotalScore () {
			return rawScore - totalPenalty;
		}

		public double getTotaltime () {
			return totalTime;
		}

		public int getNumControlsVisited () {
			return controlsVisited.size();
		}

		public void addToPath ( Vertex vertex, double timeVisited,
		                        double pointScored, double penaltyTaken ) {
			controlsVisited.add(vertex);
			timesVisited.add(timeVisited);
			pointsScored.add(pointScored - penaltyTaken);
			totalTime = timeVisited;
			rawScore += pointScored;
			totalPenalty += penaltyTaken;
		}

		public void removeRecentPath ( double pointScored, double penaltyTaken ) {
			int size = getNumControlsVisited();
			controlsVisited.remove(size - 1);
			timesVisited.remove(size - 1);
			pointsScored.remove(size - 1);
			totalTime = timesVisited.get(size - 2);
			rawScore -= pointScored;
			totalPenalty -= penaltyTaken;
		}

		public void printPath () {
			System.out.println("Total Time: " + totalTime + "   " + "Total Score: "
			    + getTotalScore() + "   " + "Raw Score: " + rawScore + "   "
			    + "Total Penalty: " + totalPenalty);
			for ( int i = 0 ; i < getNumControlsVisited() ; i++ ) {
				System.out.println(controlsVisited.get(i).getObject() + ": "
				    + timesVisited.get(i) + "   " + pointsScored.get(i));
			}

		}

		public void clonePath ( Path path2 ) {
			this.totalTime = path2.totalTime;
			this.rawScore = path2.rawScore;
			this.totalPenalty = path2.totalPenalty;
			this.controlsVisited.addAll(path2.controlsVisited);
			this.timesVisited.addAll(path2.timesVisited);
			this.pointsScored.addAll(path2.pointsScored);
		}

	}

}
