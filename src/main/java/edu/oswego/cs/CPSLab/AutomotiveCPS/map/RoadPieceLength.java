package edu.oswego.cs.CPSLab.AutomotiveCPS.map;

/**
 * Lengths of each of the road pieces that may appear on the track and conversion that may be needed.
 * The product was designed with inches in mind but conversions to centimeters are available.
 */
public enum RoadPieceLength {

    STRAIGHT(22),
    FIRSTLANECURVE(13.35),
    SECONDLANECURVE(16.10),
    THIRDLANECURVE(18.85),
    FOURTHLANECURVE(21.60);

    private double length;

    RoadPieceLength(double length) { this.length = length; }

    public double inchesLength() { return this.length; }

    public double cmLength() { return this.length * 2.54; }

}
