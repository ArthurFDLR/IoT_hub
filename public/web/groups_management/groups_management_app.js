/**
 * The App class is a controller holding the global state.
 * It creates all children controllers in render().
 */
class GroupsManagementApp extends React.Component {

	constructor(props) {
		super(props);
		console.info("GroupsManagementApp constructor()");
	}

	render() {
		console.info("GroupsManagementApp render()");
		return (
		<div className="container">
			<div className="row">
				<h3 className="big-heading">Groups management</h3>
				<hr className="col-sm-12" />
			</div>
			<div className="row">
				<div className="col-sm-12 floating">
					<Members />
				</div>
			</div>
		</div>);
	}
}

// export
window.GroupsManagementApp = GroupsManagementApp;