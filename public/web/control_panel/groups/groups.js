/**
 * The Groups controller holds the state of groups.
 * It creates its view in render().
 */
class Groups extends React.Component {

    constructor(props) {
		super(props);
		console.info("Members constructor()");
		this.state = {
			members: create_members_model([]),
		};
	}

    updateGroups = groups => {
		this.setState({
			members: create_members_model(groups)
		});
    }

	getGroups = () => {
		console.debug("RESTful: get groups");
		fetch("api/groups")
			.then(rsp => rsp.json())
			.then(groups => this.updateGroups(groups))
			.catch(err => console.error("Members: getGroups", err));
	}

	componentDidMount() {
		this.getGroups();
		window.setInterval(() => this.getGroups(), 1000);
	}

	render() {
		return (<GroupsView
			members={this.state.members} />);
	}
}

window.Groups = Groups;
