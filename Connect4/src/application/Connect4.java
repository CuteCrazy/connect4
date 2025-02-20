package application;
	
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
//import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;


public class Connect4 extends Application {
	
	private static final int TITLE_SIZE = 80;
	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	
	private boolean redMove = true;
	private Disc[][] grid = new Disc[COLUMNS][ROWS];
	
	private Pane discRoot = new Pane();
	
	
	private Parent createContent(){
		Pane root = new Pane();
		root.getChildren().add(discRoot);
		
		Shape gridShape = makeGrid();
		root.getChildren().add(gridShape);
		root.getChildren().addAll(makeColumns());
		
		return root;
	}
	
	
	private Shape makeGrid(){
		Shape shape = new Rectangle((COLUMNS + 1) * TITLE_SIZE, (ROWS + 1) * TITLE_SIZE);
		
		for (int y = 0; y < ROWS; y++) {
			for (int x = 0; x < COLUMNS; x++) {
				Circle circle = new Circle(TITLE_SIZE / 2);
				circle.setCenterX(TITLE_SIZE / 2);
				circle.setCenterY(TITLE_SIZE / 2);
				circle.setTranslateX(x * (TITLE_SIZE + 5) + TITLE_SIZE / 4);
				circle.setTranslateY(y * (TITLE_SIZE + 5) + TITLE_SIZE / 4);
				
				shape = Shape.subtract(shape, circle);
			}
		}
		Light.Distant light = new Light.Distant();
		light.setAzimuth(45.0);
		light.setElevation(30.0);
		
		Lighting lighting = new Lighting();
		lighting.setLight(light);
		lighting.setSurfaceScale(5.0);
		
		shape.setFill(Color.BLUE);
		shape.setEffect(lighting);
		
		return shape;
	}
	
	
	private List<Rectangle> makeColumns(){
		List<Rectangle> list = new ArrayList<>();
		for (int x = 0; x < COLUMNS; x++) {
			Rectangle rect = new Rectangle(TITLE_SIZE, (ROWS + 1) * TITLE_SIZE);
			rect.setTranslateX(x * (TITLE_SIZE + 5) + TITLE_SIZE / 4);
			rect.setFill(Color.TRANSPARENT);
			
			rect.setOnMouseEntered(e -> rect.setFill(Color.rgb(200, 200, 50, 0.3)));
			rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));
			
			final int column = x;
			rect.setOnMouseClicked(e -> placeDisc(new Disc(redMove), column));
			
			list.add(rect);
		}
		return list;
	}
	
	
	private void placeDisc(Disc disc, int column){
		int row = ROWS - 1;
		do{
			if(!getDisc(column, row).isPresent())
				break;
			row--;
		}while(row >= 0);
		
		if(row < 0)
			return;
		
		grid[column][row] = disc;
		discRoot.getChildren().add(disc);
		disc.setTranslateX(column * (TITLE_SIZE + 5) + TITLE_SIZE / 4);
		
		final int currentRow = row;
		
		TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5), disc);
		animation.setToY(row * (TITLE_SIZE + 5) + TITLE_SIZE / 4);
		animation.setOnFinished(e -> {
			if(gameEnded(column, currentRow)){
				gameOver();
			}
			redMove = !redMove;
		});
		animation.play();
	}
	
	
	private boolean gameEnded(int column, int row){
		List<Point2D> vertical = IntStream.rangeClosed(row - 3, row + 3)
				.mapToObj(r -> new Point2D(column, r))
				.collect(Collectors.toList());
		
		List<Point2D> horizontal = IntStream.rangeClosed(column - 3, column + 3)
				.mapToObj(c -> new Point2D(c, row))
				.collect(Collectors.toList());
		
		Point2D topLeft = new Point2D(column - 3, row - 3);
		List<Point2D> diagonal1 = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> topLeft.add(i, i))
				.collect(Collectors.toList());
		
		Point2D botLeft = new Point2D(column - 3, row + 3);
		List<Point2D> diagonal2 = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> botLeft.add(i, -i))
				.collect(Collectors.toList());
		
		return checkRange(vertical) || checkRange(horizontal) || checkRange(diagonal1) || checkRange(diagonal2);
	}
	
	
	private boolean checkRange(List<Point2D> points){
		int chain = 0;
		
		for(Point2D p : points){
			int column = (int) p.getX();
			int row = (int) p.getY();
			
			Disc disc = getDisc(column, row).orElse(new Disc(!redMove));
			if(disc.red == redMove){
				chain++;
				if(chain == 4){
					return true;
				}
			}else{
				chain = 0;
			}
		}
		return false;
	}
	
	
	private void gameOver(){
		
		Stage dialog = new Stage();
		dialog.initStyle(StageStyle.UTILITY);
		dialog.setTitle("Final Result");
//		dialog.setX(350);
//		dialog.setY(350);
		Scene scene = new Scene(new Group(new Text(50, 50, "Winner is : " + (redMove ? "RED" : "YELLOW"))));
		dialog.setScene(scene);
		dialog.show();
			
	}
	
	
	private Optional<Disc> getDisc(int column, int row){
		if(column < 0 || column >= COLUMNS
				|| row < 0 || row >= ROWS)
			return Optional.empty();
		
		return Optional.ofNullable(grid[column][row]);
	}
	
	
	public static class Disc extends Circle {
		private final boolean red;
		public Disc(boolean red){
			super(TITLE_SIZE / 2, red ? Color.RED : Color.YELLOW);
			this.red = red;
			
			setCenterX(TITLE_SIZE / 2);
			setCenterY(TITLE_SIZE / 2);
		}
		
	}
	
	
	@Override
	public void start(Stage stage) throws Exception{
		stage.setScene(new Scene(createContent()));
		stage.show();
		stage.setTitle("Connect Four");
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
