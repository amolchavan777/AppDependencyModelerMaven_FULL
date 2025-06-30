package com.modeler.app;

import java.util.*;

/**
 * Utility class for computing dependency graph metrics.
 */
public class DependencyMetrics {

    /**
     * Compute various metrics about the dependency graph and print them.
     *
     * @param result map of application -> dependencies
     */
    public static void printMetrics(Map<String, Set<String>> result) {
        Set<String> allApps = new HashSet<>(result.keySet());
        result.values().forEach(allApps::addAll);

        Map<String, Integer> outgoingCount = new HashMap<>();
        Map<String, Integer> incomingCount = new HashMap<>();
        for (String app : allApps) {
            outgoingCount.put(app, result.getOrDefault(app, Collections.emptySet()).size());
            incomingCount.put(app, 0);
        }
        for (Map.Entry<String, Set<String>> entry : result.entrySet()) {
            for (String target : entry.getValue()) {
                incomingCount.put(target, incomingCount.get(target) + 1);
            }
        }

        List<String> noOutgoing = new ArrayList<>();
        List<String> noIncoming = new ArrayList<>();
        for (String app : allApps) {
            if (outgoingCount.get(app) == 0) noOutgoing.add(app);
            if (incomingCount.get(app) == 0) noIncoming.add(app);
        }

        String maxOutApp = null, maxInApp = null;
        int maxOut = -1, maxIn = -1;
        for (String app : allApps) {
            int out = outgoingCount.get(app);
            int in = incomingCount.get(app);
            if (out > maxOut) { maxOut = out; maxOutApp = app; }
            if (in > maxIn) { maxIn = in; maxInApp = app; }
        }

        System.out.println("\n\uD83D\uDCCA Dependency Metrics:");
        System.out.println("Outgoing dependencies (fan-out) per app: " + outgoingCount);
        System.out.println("Incoming dependencies (fan-in) per app: " + incomingCount);
        System.out.println("Apps with no outgoing deps: " + (noOutgoing.isEmpty() ? "None" : noOutgoing));
        System.out.println("Apps with no incoming deps: " + (noIncoming.isEmpty() ? "None" : noIncoming));
        System.out.println("Highest fan-out: " + maxOutApp + " (" + maxOut + " outgoing links)");
        System.out.println("Highest fan-in: " + maxInApp + " (" + maxIn + " incoming links)");

        List<Set<String>> clusters = findClusters(result, allApps);
        if (clusters.size() > 1) {
            int idx = 1;
            for (Set<String> cl : clusters) {
                System.out.println("Cluster " + idx++ + ": " + cl);
            }
        }
        System.out.println(hasCycles(result) ? "Cyclic dependencies detected" : "No cycles detected");
    }

    private static List<Set<String>> findClusters(Map<String, Set<String>> graph, Set<String> allApps) {
        Map<String, Set<String>> undirected = new HashMap<>();
        for (String app : allApps) {
            undirected.put(app, new HashSet<>());
        }
        for (Map.Entry<String, Set<String>> e : graph.entrySet()) {
            String from = e.getKey();
            for (String to : e.getValue()) {
                undirected.get(from).add(to);
                undirected.computeIfAbsent(to, k -> new HashSet<>()).add(from);
            }
        }
        Set<String> visited = new HashSet<>();
        List<Set<String>> clusters = new ArrayList<>();
        for (String app : allApps) {
            if (!visited.contains(app)) {
                Set<String> cluster = new HashSet<>();
                Deque<String> stack = new ArrayDeque<>();
                stack.push(app);
                visited.add(app);
                while (!stack.isEmpty()) {
                    String cur = stack.pop();
                    cluster.add(cur);
                    for (String n : undirected.getOrDefault(cur, Collections.emptySet())) {
                        if (!visited.contains(n)) {
                            visited.add(n);
                            stack.push(n);
                        }
                    }
                }
                clusters.add(cluster);
            }
        }
        return clusters;
    }

    private static boolean hasCycles(Map<String, Set<String>> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        for (String node : graph.keySet()) {
            if (dfsCycle(node, graph, visited, stack)) return true;
        }
        return false;
    }

    private static boolean dfsCycle(String node, Map<String, Set<String>> graph,
                                    Set<String> visited, Set<String> stack) {
        if (stack.contains(node)) return true;
        if (visited.contains(node)) return false;
        visited.add(node);
        stack.add(node);
        for (String neigh : graph.getOrDefault(node, Collections.emptySet())) {
            if (dfsCycle(neigh, graph, visited, stack)) return true;
        }
        stack.remove(node);
        return false;
    }
}
