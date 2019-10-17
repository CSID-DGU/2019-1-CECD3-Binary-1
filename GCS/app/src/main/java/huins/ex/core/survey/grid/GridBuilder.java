package huins.ex.core.survey.grid;

import java.util.List;

import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.helpers.geoTools.LineCoord2D;
import huins.ex.core.polygon.Polygon;
import huins.ex.core.survey.SurveyData;

public class GridBuilder {

	private Polygon poly;
	private Double angle;
	private Double lineDist;
	private Coord2D origin;
	private Double wpDistance;

	private Grid grid;

	public GridBuilder(Polygon polygon, SurveyData surveyData, Coord2D originPoint) {
		this.poly = polygon;
		this.origin = originPoint;
		this.angle = surveyData.getAngle();
		this.lineDist = surveyData.getLateralPictureDistance();
		this.wpDistance = surveyData.getLongitudinalPictureDistance();
	}

	public GridBuilder(Polygon polygon, double angle, double distance, Coord2D originPoint) {
		this.poly = polygon;
		this.origin = originPoint;
		this.angle = angle;
		this.lineDist = distance;
		this.wpDistance = distance;
	}
	
	public void setAngle(double newAngle){
		angle = newAngle;
	}

	public Grid generate(boolean sort) throws Exception {
		List<Coord2D> polygonPoints = poly.getPoints();

		List<LineCoord2D> circumscribedGrid = new CircumscribedGrid(polygonPoints, angle, lineDist)
				.getGrid();
		List<LineCoord2D> trimedGrid = new Trimmer(circumscribedGrid, poly.getLines())
				.getTrimmedGrid();
		EndpointSorter gridSorter = new EndpointSorter(trimedGrid, wpDistance);
		gridSorter.sortGrid(origin, sort);
		grid = new Grid(gridSorter.getSortedGrid(), gridSorter.getCameraLocations());
		return grid;
	}

}
