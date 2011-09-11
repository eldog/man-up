package com.appspot.manup.signup.ui;

public final class PathPoint
{
    public final float x;
    public final float y;
    public final boolean joinToPreviousPoint;

    public PathPoint(final float x, final float y, final boolean joinToPreviousPoint)
    {
        super();
        this.x = x;
        this.y = y;
        this.joinToPreviousPoint = joinToPreviousPoint;
    } // constructor(float, float, boolean)

    public float distanceBetween(final PathPoint o)
    {
        final double dx = o.x - x;
        final double dy = o.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    } // distanceBetween(PathPoint)

} // class PathPoint
