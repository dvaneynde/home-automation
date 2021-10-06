package eu.dlvm.iohardware.diamondsys.messaging;

import java.util.StringTokenizer;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.iohardware.diamondsys.OpalmmBoard;

/**
 * Digital input measurements received from Hardware Driver, from one OPALMM board.
 * <p>
 * <code>INP_O address:int val:int\n</code>
 * <p>
 * <dl>
 * <dt>address
 * <dd>i/o address in hex format with '0x' prefix.
 * <dt>val
 * <dd>really a byte, each bit is the state on or off of one channel.
 * </dl>
 * 
 * @author dirk vaneynde
 * 
 */
public class OpalmmBoardWithMsg extends OpalmmBoard implements IBoardMessaging {

    static Logger log = LoggerFactory.getLogger(OpalmmBoardWithMsg.class);

    // For driver
    private final static char BOARDTYPE_OPALMM = 'O';

    /**
     * Constructor.
     * 
     * @param boardNumber
     *            See superclass.
     * @param address
     *            See superclass.
     * @param description
     *            See superclass.
     * @param digiInEnabled
     *            Whether digital in is enabled.
     * @param digiOutEnabled
     *            Whether digital out is enabled.
     */
    public OpalmmBoardWithMsg(int boardNumber, int address, String description, boolean digiInEnabled, boolean digiOutEnabled) {
        super(boardNumber, address, description, digiInEnabled, digiOutEnabled);
    }

    /**
     * Constructor. All inputs and outputs enabled.
     * 
     * @see OpalmmBoard#OpalmmBoard(int, int, String, boolean, boolean)
     */
    public OpalmmBoardWithMsg(int boardNumber, int address, String description) {
        this(boardNumber, address, description, true, true);
    }

    /**
     * Constructor. All inputs and outputs enabled, default description.
     * 
     * @see OpalmmBoard#OpalmmBoard(int, int, String, boolean, boolean)
     */
    public OpalmmBoardWithMsg(int boardNumber, int address) {
        this(boardNumber, address, DEFAULT_DESCRIPTION, true, true);
    }

    @Override
    public String msgInitBoard() {
        return GeneralMsg.constructBoardInit(BOARDTYPE_OPALMM, getAddress());
    }

    /**
     * Request input state of OPALMM board. The request sent is:
     * <p>
     * <code>REQ_INP 0x300 O</code><br>
     * where 0x300 is the address in hex.
     * 
     * @return line to be sent to hardware driver.
     */
    @Override
    public String msgInputRequest() {
        if (digiIn == null)
            return "";
        return String.format("REQ_INP 0x%x O\n", getAddress());
    }

    @Override
    public void parseInputResult(String sCmd, int address, StringTokenizer st) {
        if (!sCmd.equals("INP_O")) {
            log.warn("Unexpected line received from driver, command="+sCmd+", address="+getAddress()+", remaining tokens="+st.toString());
            return;
        }
        String s = st.nextToken();
        int value = Integer.parseInt(s);
        digiIn.updateInputFromHardware(value);
    }

    /**
     * Message sent to Hardware Driver, to set digital output values for a given address (=board).
     * <p>
     * <code>SET_OUT address:int type:char value:int\n</code>
     * <p>
     * An example is:<br>
     * <code>SET_OUT 0x300 O 5</code><br>
     * where 0x300 is the address, the 'O' is fixed and stands for OPALMM, 5 means digital channels 0 and 2 are to be set to ON.
     * <p>
     * Currently we do not support multiple channels per address, where each channel (or 'port' in Diamond systems speak) would have 8 digital outputs. There is
     * no need - yet.
     * 
     * @return line to be sent to hardware driver
     */
    @Override
    public String msgOutputRequest() {
        if (!outputStateHasChanged())
            return "";
        return String.format("SET_OUT 0x%x %c %d\n", address, BOARDTYPE_OPALMM, digiOut.getValue());
    }
}
