function groupAction(name, action) {
	var url = "api/groups/" + name + "?action=" + action;
	console.info("groupAction: request " + url);
	fetch(url);
}

function showGroupButtons(name) {
    return (
        <div className="btn-group btn-group-sm float-right">
            <button type="button" className="btn btn-primary" onClick={() => groupAction(name, "on")}>
                On
            </button>
            <button type="button" className="btn btn-primary" onClick={() => groupAction(name, "off")}>
                Off
            </button>
            <button type="button" className="btn btn-primary" onClick={() => groupAction(name, "toggle")}>
                Toggle
            </button>
        </div>
    );
}

/**
 * This is a stateless view showing one group.
 */
function GroupCard(props) {
    //style={{width: '50%'}}
    var name = props.groupName;
    var plugsStr = Array.from(props.plugs).join(', ');
    if (plugsStr.length == 0) {
        plugsStr = "No devices found in this group."
    }

	return (
		<div className="card" > 
            <div className="card-body">
                <div className="row align-items-center">
                    <div className="col">
                        <h5 className="small-title">{name}</h5>
                    </div>
                    <div className="col">
                        {showGroupButtons(name)}
                    </div>
                </div>
                <span className="text-info">{plugsStr}</span>
            </div>
        </div>
    );
}

/**
 * This is a stateless view listing all groups.
 */
window.GroupsView = function (props) {
	if (props.members.get_group_names().length == 0)
		return (<div>There are no groups.</div>);

	var rows = props.members.get_group_names().map(function (groupName) {
		return (
            <GroupCard key={groupName}
                groupName={groupName}
				plugs={props.members.get_group_members(groupName)}/>);
	});

	return (
		<div>
			{rows}
		</div>);
}
