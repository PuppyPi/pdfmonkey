package rebound.tieins.pdfplumberpuppetteer;

import java.util.List;

public class PDFPlumberTableMetadata
{
	protected final PDFPlumberBoundingBox boundingBox;  //.bbox in Python
	protected final List<PDFPlumberBoundingBox> cellBoundingBoxen;  //.cells in Python    (what's the plural of box, Brian? XD )
	
	public PDFPlumberTableMetadata(PDFPlumberBoundingBox boundingBox, List<PDFPlumberBoundingBox> cellBoundingBoxen)
	{
		this.boundingBox = boundingBox;
		this.cellBoundingBoxen = cellBoundingBoxen;
	}
	
	public PDFPlumberBoundingBox getBoundingBox()
	{
		return boundingBox;
	}
	
	public List<PDFPlumberBoundingBox> getCellBoundingBoxen()
	{
		return cellBoundingBoxen;
	}
}
