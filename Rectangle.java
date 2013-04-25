public class Rectangle{
	
	int upperI;
	int upperJ;
	int lowerI;
	int lowerJ;
	
	int maxBand;
	int minBand;
	int maxScore;

	public Rectangle(int upperI, int upperJ, int lowerI, int lowerJ, int maxBand, int minBand, int maxScore)
	{
		this.upperI = upperI;
		this.upperJ = upperJ;
		this.lowerI = lowerI;
		this.lowerJ = lowerJ;
		
		this.maxBand = maxBand;
		this.minBand = minBand;
        this.maxScore = maxScore;
	}

	public Rectangle() 
	{

	}
}