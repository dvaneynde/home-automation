package eu.dlvm.iohardware.diamondsys;

/**
 * A OpalmmBoard abstracts any I/O board that this system can handle. It is
 * roughly based on Diamond System boards - but probably works for a lot of
 * other boards as well.
 * <p>
 * Here are some assumptions:
 * <ol>
 * <li>A board has exactly one <code>address</code>, which typically maps to (or is) a memory
 * address.</li>
 * <li>A board can be input and/or output, digital and/or analog.</li>
 * <li>A <code>channel</code> is one input or output capability. Boards have
 * 0..N input channels, and 0..N output channels.</li>
 * <li>A board has an informal description text.</li>
 * </ol>
 * 
 * @author dirk vaneynde
 * 
 */
public abstract class Board {

	protected String description;
	protected int address;
	protected int boardNumber;

	public Board(int boardNr, int address, String description) {
		this.boardNumber = boardNr;
		this.address = address;
		this.description = description;
	}

	/**
	 * @return The fysical boardNumber, for easy visual inspection. See also
	 *         {@link FysCh}.
	 */
	public int getBoardNumber() {
		return boardNumber;
	}

	/**
	 * @return Informal description of the actual board.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Physical address of the board, typically a memory location.
	 */
	public int getAddress() {
		return address;
	}

	/**
	 * Initializes or re-initializes boards. This includes setting outputs
	 * and/or inputs to their defaults values.
	 * <p>
	 * It also must be called before {{@link #outputStateHasChanged()} can be
	 * used reliably.
	 */
	public abstract void init();

	/**
	 * @return Whether output state has changed, since last
	 *         {@link #resetOutputChangedDetection()}.
	 */
	public abstract boolean outputStateHasChanged();

	/**
	 * After this call, all calls to {{@link #outputStateHasChanged()} will
	 * return false, until at least one output state has changed for at least
	 * one
	 * channel.
	 */
	public abstract void resetOutputChangedDetection();

	@Override
	public String toString() {
		return "board nr=" + getBoardNumber() + ", desc='" + description
				+ "' address=" + address;
	}
}
