import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.javadsl.TestKit;
import com.cristian.tictactoe.PathQualityQuery;
import com.cristian.tictactoe.PathQualityQuery.PathQualityResponse;
import com.cristian.tictactoe.exceptions.BoardFullException;
import com.cristian.tictactoe.models.Board;
import com.cristian.tictactoe.models.Board.Cell;
import com.cristian.tictactoe.models.Chip;
import com.cristian.tictactoe.models.Coordinate;
import com.google.common.truth.Truth;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;

public class PathQualityQueryTest {

	private static ActorSystem actorSystem;
	private TestKit testKit;

	@BeforeClass
	public static void setUpClass() {
		actorSystem = ActorSystem.create();
	}

	@AfterClass
	public static void teardownClass() {
		TestKit.shutdownActorSystem(actorSystem, true);
		actorSystem = null;
	}

	@Before
	public void setUp() {
		testKit = new TestKit(actorSystem);
	}

	@Test
	public void testNormalEnvironment() {
		Board board = Board.empty()
						   .putChip(new Cell(Chip.X, new Coordinate(0, 0)))
						   .putChip(new Cell(Chip.O, new Coordinate(0, 1)))
						   .putChip(new Cell(Chip.X, new Coordinate(0, 2)))
						   .putChip(new Cell(Chip.O, new Coordinate(1, 0)))
						   .putChip(new Cell(Chip.X, new Coordinate(1, 1)))
						   .putChip(new Cell(Chip.O, new Coordinate(1, 2)));

		long requestId = 0;
		Props props = PathQualityQuery.props(requestId, testKit.getRef(), board, Chip.X);
		ActorRef worker = actorSystem.actorOf(props);
		testKit.watch(worker);

		PathQualityResponse pathQualityResponse = testKit.expectMsgClass(PathQualityResponse.class);
		testKit.expectTerminated(worker);

		Truth.assertThat(pathQualityResponse.getPathList()).isNotNull();
		Truth.assertThat(pathQualityResponse.getRequestId()).isEqualTo(requestId);
		Truth.assertThat(pathQualityResponse.getPathList().size()).isEqualTo(3);
	}

	@Test
	public void testFullBoard() {
		Board board = Board.empty()
						   .putChip(new Cell(Chip.X, new Coordinate(0, 0)))
						   .putChip(new Cell(Chip.O, new Coordinate(0, 1)))
						   .putChip(new Cell(Chip.X, new Coordinate(0, 2)))
						   .putChip(new Cell(Chip.O, new Coordinate(1, 0)))
						   .putChip(new Cell(Chip.X, new Coordinate(1, 1)))
						   .putChip(new Cell(Chip.O, new Coordinate(1, 2)))
						   .putChip(new Cell(Chip.X, new Coordinate(2, 0)))
						   .putChip(new Cell(Chip.O, new Coordinate(2, 1)))
						   .putChip(new Cell(Chip.X, new Coordinate(2, 2)));

		long requestId = 0;
		Props props = PathQualityQuery.props(requestId, testKit.getRef(), board, Chip.X);
		ActorRef worker = actorSystem.actorOf(props);
		testKit.watch(worker);

		Status.Failure failure = testKit.expectMsgClass(Status.Failure.class);
		testKit.expectTerminated(worker);

		Truth.assertThat(failure.cause()).isInstanceOf(BoardFullException.class);
	}

	@Test
	public void testEmptyBoard() {
		Board board = Board.empty();

		long requestId = 0;
		Props props = PathQualityQuery.props(requestId, testKit.getRef(), board, Chip.X);
		ActorRef worker = actorSystem.actorOf(props);
		testKit.watch(worker);

		PathQualityResponse pathQualityResponse = testKit.expectMsgClass(Duration.ofSeconds(20), PathQualityResponse.class);
		testKit.expectTerminated(worker);

		Truth.assertThat(pathQualityResponse.getPathList()).isNotNull();
		Truth.assertThat(pathQualityResponse.getRequestId()).isEqualTo(requestId);
		Truth.assertThat(pathQualityResponse.getPathList().size()).isEqualTo(9);
	}

}
