function plugAction(name, action) {
	var url = "api/plugs/" + name + "?action=" + action;
	console.info("plugAction: request " + url);
	fetch(url);
}

function showPlugButtons(name) {
	return (
		<div className="btn-group btn-group-sm float-right">
			<button type="button" className="btn btn-primary" onClick={() => plugAction(name, "on")}>
				On
			</button>
			<button type="button" className="btn btn-primary" onClick={() => plugAction(name, "off")}>
				Off
			</button>
			<button type="button" className="btn btn-primary" onClick={() => plugAction(name, "toggle")}>
				Toggle
			</button>
		</div>
	);
}

function showPower(power) {
	return (
		<span className="cardSpan">
			<span className="icon">
				<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="bi bi-lightning-charge" viewBox="0 0 16 16">
					<path d="M11.251.068a.5.5 0 0 1 .227.58L9.677 6.5H13a.5.5 0 0 1 .364.843l-8 8.5a.5.5 0 0 1-.842-.49L6.323 9.5H3a.5.5 0 0 1-.364-.843l8-8.5a.5.5 0 0 1 .615-.09zM4.157 8.5H7a.5.5 0 0 1 .478.647L6.11 13.59l5.732-6.09H9a.5.5 0 0 1-.478-.647L9.89 2.41 4.157 8.5z"/>
				</svg>
			</span>
			<span className="text-info">{power}</span>
		</span>
	);
}

function showState(state) {
	return (
		<span className="cardSpan">
			<span className="icon">
				<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="bi bi-power" viewBox="0 0 16 16">
					<path d="M7.5 1v7h1V1h-1z"/>
					<path d="M3 8.812a4.999 4.999 0 0 1 2.578-4.375l-.485-.874A6 6 0 1 0 11 3.616l-.501.865A5 5 0 1 1 3 8.812z"/>
				</svg>
			</span>
			<span className="text-info">{state}</span>
		</span>
	);
}

/**
 * This is a stateless view showing one plug.
 */
function PlugCard(props) {
	var name = props.plug.name;
	var state = props.plug.state;
	var power = props.plug.power;

	return (
		<div className="card" > 
            <div className="card-body">
                <div className="row align-items-center">
                    <div className="col-4">
                        <h5 className="small-title">{name}</h5>
                    </div>
					<div className="col-2">
						{showState(state)}
                    </div>
					<div className="col">
						{showPower(power)}
                    </div>
                    <div className="col">
						{showPlugButtons(name)}
                    </div>
                </div>
            </div>
        </div>
    );
}

/**
 * This is a stateless view listing all plugs.
 */
window.PlugsView = function (props) {
	if (props.plugs.length == 0)
		return (<div>No plug available.</div>);

	var rows = props.plugs.map(function (plug) {
		return (
			<PlugCard key={plug.name}
				plug={plug} />);
	});

	return (
		<div>
			{rows}
		</div>);
}
