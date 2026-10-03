package cafes.model;

public interface Algoritmo
{
	public static final int ExhaustiveSearch = 0;
	public static final int SimulatedAnnealing = 1;
	public static final int TabooSearch = 2;
	public static final int HeuristicSearch = 3;
	public static final int ManualSearch = 4;
	public static final int HeuristicSearch_SA = 5;
	public static final int HeuristicSearch_Taboo = 6;
	public static final int BadMappingSearch = 7;
	public static final int HeuristicTwoSearch = 8;
	public static final int HeuristicThreeSearch = 9;
}