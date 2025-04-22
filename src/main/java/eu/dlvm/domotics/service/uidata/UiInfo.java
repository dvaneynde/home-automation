package eu.dlvm.domotics.service.uidata;

import eu.dlvm.domotics.base.Block;

/**
 * The UiInfo class is a UI information about a domotics {@Block} for display or processing
 * purposes.
 * </p>
 * 
 * <p>
 * Fields:
 * </p>
 * <ul>
 * <li><b>name</b>: The identifying name of the UI element.</li>
 * <li><b>type</b>: The type of the UI element, typically derived from the block's class name.</li>
 * <li><b>description</b>: A textual description of the UI element. Defaults to an empty string if
 * not provided.</li>
 * <li><b>groupName</b>: The name of the group to which the UI element belongs. Defaults to an empty
 * string if not provided.</li>
 * <li><b>groupSeq</b>: The sequence number of the UI element within its group, 0 based. Defaults to 0 if not
 * provided.</li>
 * <li><b>status</b>: The status of the UI element. Defaults to an empty string if not
 * provided.</li>
 * </ul>
 * 
 * @author dirk
 * 
 */
public class UiInfo {
	private String name;
	private String type;
	private String description = "";
	private String groupName = "";
	private int groupSeq = 0;
	private String status = "";

	public UiInfo() {}

	public UiInfo(Block block, String status) {
		setName(block.getName());
		setType(block.getClass().getSimpleName());
		setDescription(block.getDescription());
		setStatus(status);

		String groupSeq = block.getUiGroup();
		if (groupSeq != null && groupSeq.contains(":")) {
			String[] groupSeqList = groupSeq.split(":");
			setGroupName(groupSeqList[0]);
			setGroupSeq(Integer.valueOf(groupSeqList[1]));
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = (description == null ? "" : description);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = (status == null ? "" : status);
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String group) {
		this.groupName = (group == null ? "" : group);
	}

	public int getGroupSeq() {
		return groupSeq;
	}

	public void setGroupSeq(int groupSeq) {
		this.groupSeq = groupSeq;
	}

	@Override
	public String toString() {
		return "UiInfo [name=" + name + ", type=" + type + ", description=" + description + ", groupName=" + groupName
				+ ", groupSeq=" + groupSeq + ", status=" + status + "]";
	}

}
