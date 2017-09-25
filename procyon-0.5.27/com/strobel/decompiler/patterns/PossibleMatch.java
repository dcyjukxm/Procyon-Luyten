package com.strobel.decompiler.patterns;

final class PossibleMatch
{
    final INode nextOther;
    final int checkPoint;
    
    PossibleMatch(final INode nextOther, final int checkPoint) {
        super();
        this.nextOther = nextOther;
        this.checkPoint = checkPoint;
    }
}
