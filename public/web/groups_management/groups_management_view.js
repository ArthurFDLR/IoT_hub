const btnClassAdd = "btn btn-primary btn-block";
const btnClassDel = "btn btn-danger btn-block";

function showGroupColumnHeader(groupName, onDeleteGroup, onAddAllMembersToGroup) {
	var onDeleteClick = () => onDeleteGroup(groupName);
	var onAddAllClick = () => onAddAllMembersToGroup(groupName);
	return (
		<div>
			<div style={{display:"inline-block", margin:"auto"}}>
				<h5 className="small-title" style={{fontSize: "18px"}} >{groupName}</h5>
			</div>
			<div className="clickable-icon" style={{display:"inline-block",margin:"auto", transform: "translate(0,-2px)"}} onClick={onAddAllClick}>
				<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" className="bi bi-check2-all" viewBox="0 0 16 16">
					<path d="M12.354 4.354a.5.5 0 0 0-.708-.708L5 10.293 1.854 7.146a.5.5 0 1 0-.708.708l3.5 3.5a.5.5 0 0 0 .708 0l7-7zm-4.208 7-.896-.897.707-.707.543.543 6.646-6.647a.5.5 0 0 1 .708.708l-7 7a.5.5 0 0 1-.708 0z"/>
					<path d="m5.354 7.146.896.897-.707.707-.897-.896a.5.5 0 1 1 .708-.708z"/>
				</svg>
			</div>
			<div className="clickable-icon" style={{display:"inline-block",margin:"auto", transform: "translate(0,-2px)"}} onClick={onDeleteClick}>
				<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="#FF2921" className="bi bi-x" viewBox="0 0 16 16">
					<path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
				</svg>
			</div>
		</div>
	);
}

/**
 * This is a stateless view showing the table header.
 */
function Header(props) {
	var ths = [];
	for (var groupName of props.groupNames) {
		ths.push(
			<th key={groupName}>
				{showGroupColumnHeader(groupName, props.onDeleteGroup, props.onAddAllMembersToGroup)}
			</th>
		);
	}

	return (
		<thead>
			<tr height="30px" valign="middle">
				<th width="20%"></th>
				{ths}
				<th width="5%"></th>
			</tr>
		</thead>
	);
}

function showRowButtons(onAddClick, onRemoveClick) {
	return (
		<div>
			<div className="clickable-icon" style={{display:"inline-block"}} onClick={onAddClick}>
				<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" className="bi bi-box-arrow-in-right" viewBox="0 0 16 16">
					<path fillRule="evenodd" d="M6 3.5a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v9a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-2a.5.5 0 0 0-1 0v2A1.5 1.5 0 0 0 6.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-9A1.5 1.5 0 0 0 14.5 2h-8A1.5 1.5 0 0 0 5 3.5v2a.5.5 0 0 0 1 0v-2z"/>
					<path fillRule="evenodd" d="M11.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 1 0-.708.708L10.293 7.5H1.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"/>
				</svg>
			</div>
			<div className="clickable-icon" style={{display:"inline-block"}} onClick={onRemoveClick}>
				<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" className="bi bi-box-arrow-right" viewBox="0 0 16 16">
					<path fillRule="evenodd" d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2a.5.5 0 0 0 1 0v-2A1.5 1.5 0 0 0 9.5 2h-8A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-2a.5.5 0 0 0-1 0v2z"/>
					<path fillRule="evenodd" d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"/>
				</svg>
			</div>
		</div>
	);
}

/**
 * This is a stateless view showing one row.
 */
function Row(props) {
	var members = props.members;
	var tds = members.get_group_names().map(groupName => {
		var onChange = () => props.onMemberChange(props.memberName, groupName);
		var checked = members.is_member_in_group(props.memberName, groupName);
		var id = props.memberName+"_"+groupName+"_check"
		return (
			<td key={groupName}>
				<div className="custom-control custom-checkbox">
					<input type="checkbox" className="custom-control-input" id={id} onChange={onChange} checked={checked}/>
					<label className="custom-control-label" htmlFor={id}/>
				</div>
			</td>
		);
	});

	var onAddClick = () => props.onAddMemberToAllGroups(props.memberName);
	var onRemoveClick = () => props.onRemoveMemberFromAllGroups(props.memberName);
	return (
		<tr>
			<td><div className="text-info">{props.memberName}</div></td>
			{tds}
			<td> {showRowButtons(onAddClick, onRemoveClick)} </td>
		</tr>
	);
}

/**
 * This is a stateless view showing inputs for add/replace groups.
 */
function AddGroup(props) {
	var onChangeName = event => props.onInputNameChange(event.target.value);
	return (
		<div className="container">
			<div className="row">
				<div style={{display:"inline-block", marginTop:"auto", marginBottom:"auto"}}>
					<input type="text" onChange={onChangeName} value={props.inputName} placeholder="Group name"/>
				</div>
				<div className="clickable-icon" style={{display:"inline-block"}} onClick={props.onAddGroup}>
					<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" fill="currentColor" className="bi bi-plus" viewBox="0 0 16 16">
						<path d="M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4z"/>
					</svg>
				</div>
			</div>
		</div>
	);
}

/**
 * This is a stateless view showing the table body.
 */
function Body(props) {
	var rows = props.plugNames.map(memberName =>
		<Row key={memberName} memberName={memberName} members={props.members}
			onMemberChange={props.onMemberChange}
			onAddMemberToAllGroups={props.onAddMemberToAllGroups}
			onRemoveMemberFromAllGroups={props.onRemoveMemberFromAllGroups} />);

	return (
		<tbody>
			{rows}
		</tbody>
	);
}

/**
 * This is a stateless view showing the whole members table.
 */
function MembersTable(props) {
	if (props.members.get_group_names().length == 0) {
		return (
			<div>
				<AddGroup inputName={props.inputName} inputMembers={props.inputMembers}
					onInputNameChange={props.onInputNameChange}
					onInputMembersChange={props.onInputMembersChange}
					onAddGroup={props.onAddGroup} />
				<div>There are no groups.</div>
			</div>
			);
	}
	return (
		<div>
			<AddGroup inputName={props.inputName} inputMembers={props.inputMembers}
				onInputNameChange={props.onInputNameChange}
				onInputMembersChange={props.onInputMembersChange}
				onAddGroup={props.onAddGroup} />
			<table className="table table-striped borderless">
				<Header 
					groupNames={props.members.get_group_names()} 
					onDeleteGroup={props.onDeleteGroup}
					onAddAllMembersToGroup={props.onAddAllMembersToGroup}/>
				<Body 
					members={props.members}
					plugNames={props.plugNames}
					inputName={props.inputName} inputMembers={props.inputMembers}
					onMemberChange={props.onMemberChange}
					onDeleteGroup={props.onDeleteGroup}
					onInputNameChange={props.onInputNameChange}
					onInputMembersChange={props.onInputMembersChange}
					onAddGroup={props.onAddGroup}
					onAddMemberToAllGroups={props.onAddMemberToAllGroups}
					onRemoveMemberFromAllGroups={props.onRemoveMemberFromAllGroups} />
			</table>
		</div>
		);
}

//export
window.MembersTable = MembersTable;