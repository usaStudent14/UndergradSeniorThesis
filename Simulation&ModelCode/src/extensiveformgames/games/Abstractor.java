package extensiveformgames.games;

/**
 * Game-theoretic action abstractor by Christian Kroer (https://github.com/ChrKroer/ExtensiveFormGames)
 * @author Christian Kroer
 *
 */
public interface Abstractor {
	/**
	 * indexed as map[playerId][informationSetId]
	 * @return a mapping where indices are informationSetIds and the values are the information set ids mapped to
	 */
	public int[][] informationSetMapping();
	/**
	 * indexed as map[playerId][informationSetId][actionId]
	 * @return a map[playerId][informationSetId][actionId] that returns the actionId mapped to
	 */
	public int[][][] actionMapping();
}
