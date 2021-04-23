/**
 * A model for managing members in groups.
 */
function create_members_model(groups) {
	// create the data structure
	var all_members = new Set(); // all unique member names
	var group_names = [];
	var group_members = new Map(); // group_name to set of group members 
	for (var group of groups) {
		group_names.push(group.name);
		var members = new Set();
		for (var m of group.members) {
			members.add(m.name);
		}
		group_members.set(group.name, members);
		members.forEach(member => all_members.add(member));
	}
	var member_names = Array.from(all_members);
	group_names.sort();
	member_names.sort();

	// create the object
	var that = {}
	that.get_group_names = () => group_names;
	that.get_member_names = () => member_names;
	that.is_member_in_group = (member_name, group_name) =>
		!group_members.has(group_name)? false:
			group_members.get(group_name).has(member_name);
	that.get_group_members = group_name => group_members.get(group_name);

	console.debug("Members Model",
		groups, group_names, member_names, group_members);

	return that;
}

/**
 * The Members controller holds the state of groups.
 * It creates its view in render().
 */
class Members extends React.Component {

	constructor(props) {
		super(props);
		console.info("Members constructor()");
		this.state = {
			members: create_members_model([]),
			plugs: [],
			inputName: "",
			inputMembers: "",
		};
	}

	componentDidMount() {
		console.info("Members componentDidMount()");
		this.getGroups();
		this.getPlugs();
		window.setInterval(() => this.getPlugs(), 1000);
		window.setInterval(() => this.getGroups(), 1000);
		//setInterval(this.getGroups, 1000);
	}

	render() {
		//console.info("Members render()");
		return (<MembersTable members={this.state.members}
			plugNames={this.state.plugs}
			inputName={this.state.inputName} inputMembers={this.state.inputMembers}
			onMemberChange={this.onMemberChange}
			onDeleteGroup={this.onDeleteGroup}
			onInputNameChange={this.onInputNameChange}
			onInputMembersChange={this.onInputMembersChange}
			onAddGroup={this.onAddGroup}
			onAddMemberToAllGroups={this.onAddMemberToAllGroups}
			onRemoveMemberFromAllGroups={this.onRemoveMemberFromAllGroups}
			onAddAllMembersToGroup={this.onAddAllMembersToGroup}/>);
	}

	getGroups = () => {
		console.debug("RESTful: get groups");
		fetch("api/groups")
			.then(rsp => rsp.json())
			.then(groups => this.showGroups(groups))
			.catch(err => console.error("Members: getGroups", err));
	}

	getPlugs() {
		fetch("api/plugs")
			.then(rsp => rsp.json())
			.then(data => this.updatePlugs(data))
			.catch(err => console.debug("Plugs: error " + JSON.stringify(err)));
	}

	updatePlugs(plugs) {
		if (!Array.isArray(plugs)) {
			console.debug("Plugs: cannot get plugs " + JSON.stringify(plugs));
			return;
		}
		
		var names = [];
		for (var plug of plugs) {
			names.push(plug.name);
		}
		names.sort();
		console.debug("Plugs: " + JSON.stringify(plugs));
		this.setState({ plugs: names });

		if (this.props.plugSelected == null)
			return;
	}

	showGroups = groups => {
		this.setState({
			members: create_members_model(groups)
		});
	}

	createGroup = (groupName, groupMembers) => {
		console.info("RESTful: create group "+groupName
			+" "+JSON.stringify(groupMembers));
		
		// Remove first element if it's not a proper name
		if (groupMembers.length>0 && groupMembers[0].length == 0 ) {
			groupMembers.shift();
		}

		var postReq = {
			method: "POST",
			headers: {"Content-Type": "application/json"},
			body: JSON.stringify(groupMembers)
		};
		fetch("api/groups/"+groupName, postReq)
			.then(rsp => this.getGroups())
			.catch(err => console.error("Members: createGroup", err));
	}

	createManyGroups = groups => {
		console.info("RESTful: create many groups "+JSON.stringify(groups));
		var pendingReqs = groups.map(group => {
			var postReq = {
				method: "POST",
				headers: {"Content-Type": "application/json"},
				body: JSON.stringify(group.members)
			};
			return fetch("api/groups/"+group.name, postReq);
		});

		Promise.all(pendingReqs)
			.then(() => this.getGroups())
			.catch(err => console.error("Members: createManyGroup", err));
	}

	deleteGroup = groupName => {
		console.info("RESTful: delete group "+groupName);
	
		var delReq = {
			method: "DELETE"
		};
		fetch("api/groups/"+groupName, delReq)
			.then(rsp => this.getGroups())
			.catch(err => console.error("Members: deleteGroup", err));
	}

	onMemberChange = (memberName, groupName) => {
		var groupMembers = new Set(this.state.members.get_group_members(groupName));
		if (groupMembers.has(memberName))
			groupMembers.delete(memberName);
		else
			groupMembers.add(memberName);

		this.createGroup(groupName, Array.from(groupMembers));
	}

	onDeleteGroup = groupName => {
		this.deleteGroup(groupName);
	}

	onInputNameChange = value => {
		console.debug("Members: onInputNameChange", value);
		this.setState({inputName: value});
	}

	onInputMembersChange = value => {
		console.debug("Members: onInputMembersChange", value);
		this.setState({inputMembers: value});
	}

	onAddGroup = () => {
		var name = this.state.inputName;
		var members = this.state.inputMembers.split(',');
	
		this.createGroup(name, members);
	}

	onAddMemberToAllGroups = memberName => {
		var groups = [];
		for (var groupName of this.state.members.get_group_names()) {
			var groupMembers = new Set(this.state.members.get_group_members(groupName));
			groupMembers.add(memberName);
			groups.push({name: groupName, members: Array.from(groupMembers)});
		}
		this.createManyGroups(groups);
	}

	onRemoveMemberFromAllGroups = memberName => {
		var groups = [];
		for (var groupName of this.state.members.get_group_names()) {
			var groupMembers = new Set(this.state.members.get_group_members(groupName));
			groupMembers.delete(memberName);
			groups.push({name: groupName, members: Array.from(groupMembers)});
		}
		this.createManyGroups(groups);
	}

	onAddAllMembersToGroup = groupName => {
		this.createGroup(groupName, this.state.plugs);
	}
}

// export
window.Members = Members;