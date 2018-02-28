'use strict';

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom');
const client = require('./client');
// end::vars[]

// tag::app[]
class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {educations: [], lvs: [], lv: '', education: {}};
        this.onChangeLv = this.onChangeLv.bind(this);
        this.onChangeEd = this.onChangeEd.bind(this);
    }

    componentDidMount() {
        client({method: 'GET', path: '/api/educations'}).done(response => {
            this.setState({educations: response.entity.educations});
        });
    }

    onChangeEd(event) {
        console.log("fetching lvs");
        client({method: 'GET', path: '/api/available_lvs/' + event.target.value}).done(response => {
            this.setState({lvs: response.entity, lv: response.entity[0]});
            document.getElementById("lv").value = response.entity[0];
        });
        client({method: 'GET', path: '/api/by_id/' + event.target.value + "?lv=" + this.state.lv}).done(response => {
            this.setState({education: response.entity});
        });
    }

    onChangeLv(event) {
        this.setState({lv: event.target.value});
        client({method: 'GET', path: '/api/by_id/' + this.state.education.id + "?lv=" + event.target.value}).done(response => {
            this.setState({education: response.entity});
        });
    }

    render() {
        console.log("rendering education");
        var educationOptions = this.state.educations.map(ed =>
            <option key={ed.id} value={ed.id}>{ed.name.fi}</option>
        );

        var options = [];

        if(this.state.lvs.length > 0) {
            options = this.state.lvs.map(lv =>
                <option key={lv} value={lv}>{lv}</option>
            );
        }

        return (
            <ul>
                <li>
                    <select id="ed" name="ed" onChange={this.onChangeEd}>
                        {educationOptions}
                    </select>
                </li>
                {this.state.lvs.length > 0 &&
                    <li>
                        <select id="lv" name="lv" onChange={this.onChangeLv}>
                            {options}
                        </select>
                    </li>
                }
                <li>Education</li>
                {this.state.lv != undefined && <Element key={this.state.education.id} id={this.state.education.id} elem={this.state.education} lv={this.state.lv}/>}
            </ul>
        )
    }
}

class ElementList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {elements: []}
    }

    componentDidMount() {
        if (this.props.ids != null && this.props.ids.length > 0) {
            fetch('/api/all_ids?lv=' + (this.props.lv == undefined ? '' : this.props.lv), {
                method: 'post',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(this.props.ids)
            }).then((response) => response.json()).then(responseJson => {
                this.setState({elements: responseJson});
            });
        }
    }

    componentWillReceiveProps(nextProps) {
        //console.log("elementlist " + this.props.id + " received new props");
        //console.log(nextProps);
        //console.log(this.props);

        if(isEqual(this.props, nextProps)) {
            //console.log("elementlist " + this.props.id + " arrays are equal, do nothing");
            return;
        }
        //console.log("elementlist " + this.props.id + " arrays not equal, get new with lv " + nextProps.lv);

        fetch('/api/all_ids?lv=' + (nextProps.lv == undefined ? '' : nextProps.lv), {
            method: 'post',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(nextProps.ids)
        }).then((response) => response.json()).then(responseJson => {
            this.setState({elements: responseJson});
        });
    }


    componentDidUpdate() {
    }

    render() {
        var elements = this.state.elements.map(elem =>
            <Element key={elem.id} id={elem.id} elem={elem} lv={this.props.lv}/>);
        return (
            <li>
                id: {this.props.id}
                {elements}
            </li>
        )
    }
}

class Element extends React.Component {

    componentWillReceiveProps(nextProps) {
        console.log("element " + this.props.id + " received updated properties");
        console.log(nextProps);
        console.log(this.props);
    }

    componentDidUpdate() {
        console.log("element updated: " + this.props.elem.id + ", lv: " + this.props.lv);
    }

    render() {
        var elem = this.props.elem;
        switch(elem.type) {
            case 'Education':
                return this.renderEducation(elem);
            case 'DegreeProgramme':
                //return this.renderDegreeProgramme(elem);
            case 'GroupingModule':
                //return this.renderGroupingModule(elem);
            case 'StudyModule':
                //return this.renderStudyModule(elem);
                return this.renderOtherTypes(elem);
            default:
                return (
                    <ul>
                        <li>
                            tuntematon tyyppi {elem.type}
                        </li>
                    </ul>
                )
        }
    }

    renderEducation(elem) {

        var structure = getElementStructure(elem.structure, this.props.lv);
        console.log("stucture");
        console.log(structure);

        return(
            <ul>
                <li><b>{elem.name.fi}</b> (Education)</li>
                <li>id: {elem.id} </li>
                <li>
                    Tutkinnon rakenne<br/>
                    {structure}
                </li>
            </ul>
        )
    }

    renderOtherTypes(elem) {
        var rules = parseRule(elem.rule);
        return (
            <ul>
                <li><b>{elem.name.fi}</b></li>
                <li>id: {elem.id}</li>
                <li>{elem.type}</li>
                {elem.targetCredits != null && ((elem.targetCredits.min != elem.targetCredits.max) ? <li>Opintopisteet {elem.targetCredits.min} - {elem.targetCredits.max}</li> : <li>Opintopisteet {elem.targetCredits.max}</li>)}
                {rules.modules.length > 0 &&
                <li>
                    Osat<br/>
                    <ul>
                        <ElementList key={'mods-' + elem.id} id={'mods-' + elem.id} ids={rules.modules} lv={this.props.lv}/>
                    </ul>
                </li>}
                {rules.courses.length > 0 &&
                    <li>
                        Opintojaksot<br/>
                        <CourseList key={'cu-' + elem.id} ids={rules.courses} lv={this.props.lv}/>
                    </li>
                }
            </ul>
        )
    }

    //renderDegreeProgramme(elem) {
    //    var rules = parseRule(elem.rule);
    //    console.log("groupingmodule: " + elem.id);
    //    console.log(rules);
    //    // parse elements from list
    //    return (
    //        <ul>
    //            <li><b>{elem.name.fi}</b></li>
    //            <li>{elem.type}</li>
    //            <li>Opintoviikot {elem.targetCredits.min} - {elem.targetCredits.max}</li>
    //            <li>
    //                Osat:<br/>
    //                <ul>
    //                    <ElementList key={'mods-' + elem.id} ids={rules.modules}/>
    //                </ul>
    //            </li>
    //        </ul>
    //    )
    //}
    //
    //renderGroupingModule(elem) {
    //    var rules = parseRule(elem.rule);
    //    console.log("groupingmodule: " + elem.id)
    //    console.log(rules);
    //    return (
    //        <ul>
    //            <li><b>{elem.name.fi}</b></li>
    //            <li>{elem.type}</li>
    //            <li>
    //                Osat:<br/>
    //                <ul>
    //                    <ElementList key={'mods-' + elem.id} ids={rules.modules}/>
    //                </ul>
    //            </li>
    //        </ul>
    //    )
    //}
    //
    //renderStudyModule(elem) {
    //    var rules = parseRule(elem.rule);
    //    return (
    //        <ul>
    //            <li><b>{elem.name.fi}</b></li>
    //            <li>{elem.type}</li>
    //            <li>Opintoviikot {elem.targetCredits.min} - {elem.targetCredits.max}</li>
    //            {rules.modules.length > 0 &&
    //                <li>
    //                Osat:<br/>
    //                <ul>
    //                    <ElementList key={'mods-' + elem.id} ids={rules.modules}/>
    //                </ul>
    //            </li>}
    //            <li>
    //                Opintojaksot: <br/>
    //                {rules.courses}
    //            </li>
    //        </ul>
    //    )
    //}

}

function isEqual(value, other) {

    // Get the value type
    var type = Object.prototype.toString.call(value);

    // If the two objects are not the same type, return false
    if (type !== Object.prototype.toString.call(other)) return false;

    // If items are not an object or array, return false
    if (['[object Array]', '[object Object]'].indexOf(type) < 0) return false;

    // Compare the length of the length of the two items
    var valueLen = type === '[object Array]' ? value.length : Object.keys(value).length;
    var otherLen = type === '[object Array]' ? other.length : Object.keys(other).length;
    if (valueLen !== otherLen) return false;

    // Compare two items
    var compare = function (item1, item2) {

        // Get the object type
        var itemType = Object.prototype.toString.call(item1);

        // If an object or array, compare recursively
        if (['[object Array]', '[object Object]'].indexOf(itemType) >= 0) {
            if (!isEqual(item1, item2)) return false;
        }

        // Otherwise, do a simple comparison
        else {

            // If the two items are not the same type, return false
            if (itemType !== Object.prototype.toString.call(item2)) return false;

            // Else if it's a function, convert to a string and compare
            // Otherwise, just compare
            if (itemType === '[object Function]') {
                if (item1.toString() !== item2.toString()) return false;
            } else {
                if (item1 !== item2) return false;
            }

        }
    };

    // Compare properties
    if (type === '[object Array]') {
        for (var i = 0; i < valueLen; i++) {
            if (compare(value[i], other[i]) === false) return false;
        }
    } else {
        for (var key in value) {
            if (value.hasOwnProperty(key)) {
                if (compare(value[key], other[key]) === false) return false;
            }
        }
    }

    // If nothing failed, return true
    return true;

};

function parseRule(rule) {
    var modules = [];
    var courses = [];
    var response;
    if(rule.rule != null) {
        response = parseRule(rule.rule);
        modules = modules.concat(response.modules);
        courses = courses.concat(response.courses);
    } else if(rule.rules != null) {
        for(var i = 0; i < rule.rules.length; i++) {
            response = parseRule(rule.rules[i]);
            modules = modules.concat(response.modules);
            courses = courses.concat(response.courses);
        }
    } else {
        if(rule.type == 'ModuleRule') {
            modules.push(rule.moduleGroupId);
        } else if(rule.type == 'CourseUnitRule') {
            courses.push(rule.courseUnitGroupId);
        }
    }
    return { modules: modules, courses: courses }
}

function getRules(rule) {
    var rules = [];
    if(rule.type == 'CompositeRule') {
        var subModIds = [];
        for(var i = 0; i < rule.rules.length; i++) {
            var sub = rule.rules[i];
            if(sub.type == 'CompositeRule') {
                rules.concat(getRules(sub));
            }
            if(sub.type == 'ModuleRule') {
                subModIds.push(sub.moduleGroupId);
            }
        }
        rules.push(<li key={'l-' + rule.localId}><ElementList key={rule.localId} id={rule.localId} ids={subModIds}/></li>);
    } else if(rule.type == 'ModuleRule') {
        var mods = [];
        mods.push(rule.moduleGroupId);
        rules.push(<li key={'l-' + rule.localId}><ElementList key={rule.localId} id={rule.localId} ids={mods}/></li>);
    }
    return rules;
}

function getElementStructure(struct, lv) {
    var structures = [];
    for(var property in struct) {
        if(property.startsWith("phase") && struct[property] != null)  {
            var phase = struct[property];
            var options = [];
            for(var i = 0; i < phase.options.length; i++) {
                options.push(phase.options[i].moduleGroupId);
            }
            structures.push(<ul key={property}>
                <li>{phase.name.fi}</li>
                <li><ElementList key={'opt-' + property} id={'opt-' + property} ids={options} lv={lv}/></li>
            </ul>)
        }
    }
    return <ul>{structures}</ul>;

}

class CourseList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {courseNames: []}
    }

    componentDidMount() {
        if (this.props.ids != null && this.props.ids.length > 0) {
            fetch('/api/cu/names?lv=' + (this.props.lv != undefined ? this.props.lv : ''), {
                method: 'post',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(this.props.ids)
            }).then((response) => response.json()).then(responseJson => {
                this.setState({courseNames: responseJson});
            });
        }
    }

    componentWillReceiveProps(nextProps) {
        if(isEqual(this.props, nextProps)) {
            return;
        }
        fetch('/api/cu/names?lv=' + this.props.lv, {
            method: 'post',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(nextProps.ids)
        }).then((response) => response.json()).then(responseJson => {
            this.setState({courseNames: responseJson});
        });
    }


    render() {
        var courseNames = this.state.courseNames.map((node, index) => <li key={index + "" + node.name.fi}>{node.name.fi}&nbsp;
            ({(node.credits.min == node.credits.max) ?
                (<b>{node.credits.min}</b>) :
                (<b>{node.credits.min}-{node.credits.max}</b>)}op)</li>);
        return (
            <ul>
                {courseNames}
            </ul>
        )
    }


}

// tag::render[]
ReactDOM.render(
    <App />,
    document.getElementById('react')
)
// end::render[]
