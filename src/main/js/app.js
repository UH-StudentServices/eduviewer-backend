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
        this.updateEducation(event.target.value);
    }

    updateEducation(educationId) {
        if(educationId != null && this.state.education != null && this.state.education['id'] == educationId) {
            return;
        }
        if(educationId != null) {
            this.setState({lv: undefined});
            client({method: 'GET', path: '/api/available_lvs/' + educationId}).done(response => {
                this.setState({lvs: response.entity});
                console.log("first entity: " + response.entity[0]);
                this.setState({lv: response.entity[0]})
            });
        }
        if(educationId == null) {
            educationId = this.state.education['id'];
            console.log("educationId now: ");
            console.log(educationId);
        }
        client({method: 'GET', path: '/api/by_id/' + educationId + "?lv=" + (this.state.lv == undefined ? null : this.state.lv)}).done(response => {
            this.setState({education: response.entity});
        });
    }

    onChangeLv(event) {
        this.setState({lv: event.target.value});
        this.updateEducation();
/*
        client({metdhod: 'GET', path: '/api/update_lv/' + event.target.value}).done(response => {

        });
*/
    }

    render() {
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
                <li>Educations</li>
                {this.state.lv != undefined && <Element key={this.state.education.id} elem={this.state.education} lv={this.state.lv}/>}
            </ul>
        )
    }
}

class ElementList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {elements: [], lv: props.lv}
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
        if(arraysEqual(this.props.ids, nextProps.ids)) {
            return;
        }
        if (nextProps.ids != null && nextProps.ids.length > 0) {
            fetch('/api/all_ids?lv=' + (nextProps.lv == undefined ? '' : nextProps.lv), {
                method: 'post',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(nextProps.ids)
            }).then((response) => response.json()).then(responseJson => {
                this.setState({elements: responseJson});
            });
        }
    }


    componentDidUpdate() {
        if(this.props.elements != null && this.state.elements.length == 0) {
            console.log("elements updated");
            this.setState({elements: this.props.elements});
        }
        if(this.props.lv != this.state.lv) {
            this.setState({lv: this.props.lv})
        }
    }

    render() {
        var elements = this.state.elements.map(elem =>
            <Element key={elem.id} elem={elem} lv={this.state.lv}/>);
        return (
            <li>
                id: {this.props.id}
                {elements}
            </li>
        )
    }
}

class Element extends React.Component {

    componentDidUpdate() {
        console.log("element updated: " + this.props.elem.id);
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

        var structure = getElementStructure(elem.structure);
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
                {elem.targetCredits != null && <li>Opintoviikot {elem.targetCredits.min} - {elem.targetCredits.max}</li>}
                {rules.modules.length > 0 &&
                <li>
                    Osat:<br/>
                    <ul>
                        <ElementList key={'mods-' + elem.id} id={'mods-' + elem.id} ids={rules.modules} lv={this.props.lv}/>
                    </ul>
                </li>}
                {rules.courses.length > 0 &&
                    <li>
                        Kurssit: <br/>
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
    //                Kurssit: <br/>
    //                {rules.courses}
    //            </li>
    //        </ul>
    //    )
    //}

}

function arraysEqual(a, b) {
    if (a === b) return true;
    if (a == null || b == null) return false;
    if (a.length != b.length) return false;

    // If you don't care about the order of the elements inside
    // the array, you should sort both arrays here.

    for (var i = 0; i < a.length; ++i) {
        if (a[i] !== b[i]) return false;
    }
    return true;
}

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

function getElementStructure(struct) {
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
                <li><ElementList key={'opt-' + property} id={'opt-' + property} ids={options}/></li>
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
        if(arraysEqual(this.props, nextProps)) {
            return;
        }
        if (nextProps.ids != null && nextProps.ids.length > 0) {
            fetch('/api/cu/names?lv=' + (this.props.lv != undefined ? this.props.lv : ''), {
                method: 'post',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(nextProps.ids)
            }).then((response) => response.json()).then(responseJson => {
                this.setState({courseNames: responseJson});
            });
        }
    }


    render() {
        var courseNames = this.state.courseNames.map((name, index) => <li key={index + "" + name.fi}>{name.fi}</li>);
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
