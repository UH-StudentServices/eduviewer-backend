'use strict';

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom');
const client = require('./client');
const qs = (function(a) {
    if (a == "") return {};
    var b = {};
    for (var i = 0; i < a.length; ++i)
    {
        var p=a[i].split('=', 2);
        if (p.length == 1)
            b[p[0]] = "";
        else
            b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
    }
    return b;
})(window.location.search.substr(1).split('&'));
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
        this.setState({lvs: [], lv: ''});
        client({method: 'GET', path: '/api/available_lvs/' + event.target.value}).done(response => {
            this.setState({lvs: response.entity, lv: response.entity[0]});
            document.getElementById("lv").value = response.entity[0];
        });
        client({
            method: 'GET',
            path: '/api/by_id/' + event.target.value + "?lv=" + this.state.lv
        }).done(response => {
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
                    <select id="lv" name="lv" onChange={this.onChangeLv}>
                        <option value="">---</option>
                        {options}
                    </select>
                </li>
                <li>
                    <select id="ed" name="ed" onChange={this.onChangeEd}>
                        {educationOptions}
                    </select>
                </li>
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
        if(isEqual(this.props, nextProps)) {
            return;
        }
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
            <div>
                {qs['debug'] == 'true' && <div>id: {this.props.id}</div>}
                {elements}
            </div>

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
                {qs['debug'] == 'true' && <li>id: {elem.id} </li>}
                <li>
                    Tutkinnon rakenne<br/>
                    {structure}
                </li>
            </ul>
        )
    }

    renderOtherTypes(elem) {
        var rules = parseRuleData(elem.rule);
        return (
            <ul>
                {this.renderElementHeader(elem)}
                <Rule key={'rule-' + elem.rule.localId} rule={elem.rule} lv={this.props.lv}/>
            </ul>
        )
    }

    /*{rules.modules.length > 0 &&
     <li>
     Osat<br/>
     <ul>
     <ElementList key={'mods-' + elem.id} id={'mods-' + elem.id} ids={rules.modules} lv={this.props.lv} rule={elem.rule}/>
     </ul>
     </li>}
     {rules.courses.length > 0 && elem.rule.require != null && elem.rule.require.min < rules.courses.length && <div>dropdown</div>}
     {rules.courses.length > 0 && (elem.rule.require == null || elem.rule.require.min >= rules.courses.length) &&
     <li>
     Opintojaksot<br/>
     <CourseList key={'cu-' + elem.id} ids={rules.courses} lv={this.props.lv}/>
     </li>
     }*/

    renderElementHeader(elem) {
        return (
            <div>
                <li><b>{elem.name.fi}</b></li>
                {qs['debug'] == 'true' && <li>id: {elem.id}</li>}
                {qs['debug'] == 'true' && <li>{elem.type}</li>}
                {elem.targetCredits != null && ((elem.targetCredits.min != elem.targetCredits.max) ? <li>Opintopistevaatimukset {elem.targetCredits.min} - {elem.targetCredits.max}op</li> : <li>Opintopistevaatimukset {elem.targetCredits.min}op</li>)}
            </div>)
    }


}

class Rule extends React.Component {
    render() {
        var rule = this.props.rule;
        if(rule.type == 'CompositeRule') {
            return (<CompositeRule key={rule.id} rule={rule} lv={this.props.lv}/>);
        } else if(rule.type == 'CreditsRule') {
            return (<CreditsRule key={rule.id} rule={rule} lv={this.props.lv}/>);
        } else if(rule.type == 'AnyCourseUnitRule') {
            return (<li>Mikä tahansa opintojakso</li>)
        } else if(rule.type == 'AnyModuleRule') {
            return (<li>Mikä tahansa opintokokonaisuus</li>)
        }
    }
}

class CompositeRule extends React.Component {

    constructor(props) {
        super(props);
        this.createMarkUp = this.createMarkUp.bind(this);
        this.renderModules = this.renderModules.bind(this);
    }

    isModules(array) {
        return array.length == 0 || array[0].type == 'ModuleRule';
    }

    createMarkUp() {
        return {__html: this.props.rule.description.fi}
    }

    renderModules(rule, rulesData) {
        return (<div>
            {rule.description != null && isNotEmpty(this.props.rule.description.fi) &&
            <li>
                <div dangerouslySetInnerHTML={this.createMarkUp()}></div>
            </li>
            }
            {rule.require != null && rule.require.min < rulesData.modules.length &&
            <li>
                <Dropdown key={'dd-' + rule.localId} rule={rule} ids={rulesData.modules} lv={this.props.lv}/>
            </li>
            }
            {rule.require == null &&
            <ElementList key={'mods-' + rule.localId} id={'mods-' + rule.localId} ids={rulesData.modules}
                         lv={this.props.lv} rule={rule}/>
            }
        </div>)
    }

    renderCourses(rule, rulesData) {
        return (
            <div>
                <li> Opintojaksot </li>
                <CourseList key={'cu-' + rule.id} ids={rulesData.courses} lv={this.props.lv}/>
            </div>
        )
    }

    render() {
        var rule = this.props.rule;
        var rulesData = parseRuleData(rule);

        if(rulesData.anyMR != null) {
            console.log("anyrule was true");
            console.log(rulesData);
        }

        return (
            <div>
                {rulesData.courses.length > 0 && this.renderCourses(rule, rulesData)}
                {rulesData.modules.length > 0 && this.renderModules(rule, rulesData)}
                {rulesData.anyMR != null && <Rule key={rulesData.anyMR.localId} rule={rulesData.anyMR} lv={this.props.lv}/>}
                {rulesData.anyCUR != null && <Rule key={rulesData.anyCUR.localId} rule={rulesData.anyCUR} lv={this.props.lv}/>}
                {rulesData.creditsRules.length > 0 && <Rule key={rulesData.creditsRules[0].localId} rule={rulesData.creditsRules[0]} lv={this.props.lv}/>}
            </div>
        )
    }
}

class CreditsRule extends React.Component {

    creditsRow(credits) {
        var creditsRow = "";
        if(credits.max == null || credits.min == credits.max) {
            creditsRow = credits.min + "op";
        } else {
            creditsRow = credits.min + "-" + credits.max + "op";
        }
        return (
            <li>Valitse {creditsRow}</li>
        )
    }

    render() {
        var rule = this.props.rule;
        var credits = rule.credits;
        return (
            <ul>
                {this.creditsRow(credits)}
                {<Rule key={rule.rule.localId} rule={rule.rule} lv={this.props.lv}/>}
            </ul>
        )
    }
}

class Dropdown extends React.Component {

    constructor(props) {
        super(props);
        this.state = {elements: [], selected: {}};
        this.onChange = this.onChange.bind(this);
    }

    componentDidMount() {
        fetch('/api/all_ids?lv=' + this.props.lv, {
            method: 'post',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(this.props.ids)
        }).then((response) => response.json()).then(responseJson => {
            this.setState({elements: responseJson});
        });
    }

    onChange(event) {
        this.setState({selectedId: [event.target.value]})
    }


    render() {
        if(this.state.elements.length == 0) {
            return (<div>...</div>);
        }
        var options = this.state.elements.map(element =>
            <option key={element.id} value={element.id}>{element.name.fi}</option>
        );

        var selected = getSelectValues(document.getElementById("select-" + this.props.id));

        return (
            <div>
                <select key="test" id={'select-' + this.props.id} onChange={this.onChange}>
                    <option key="" value="">---</option>
                    {options}
                </select>
                <br/>
                <ElementList key={'mods-' + this.props.rule.localId} id={'mods-' + this.props.rule.localId}
                             ids={selected} lv={this.props.lv} rule={this.props.rule}/>
            </div>

        )
    }
}

function getSelectValues(select) {
    var result = [];
    var options = select && select.options;
    if(options == null) {
        return [];
    }
    var opt;

    for (var i=0; i < options.length; i++) {
        opt = options[i];

        if (opt.selected) {
            console.log("opt: ");
            console.log(opt);
            result.push(opt.value);
        }
    }
    return result;
}


function isNotEmpty(html) {
    var div = document.createElement("div");
    div.innerHTML = html;
    var text = div.textContent || div.innerText || "";
    if(text == null || text.trim().length == 0) {
        return false;
    }
    return true;
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

function parseRuleData(rule) {
    var modules = [];
    var courses = [];
    var creditsRules = [];
    var anyCUR = null;
    var anyMR = null;
    var response;
    if(rule.rule != null) {
        response = parseRuleData(rule.rule);
        modules = modules.concat(response.modules);
        courses = courses.concat(response.courses);
        if(anyCUR == null) {
            anyCUR = response.anyCUR;
        }
        if(anyMR == null) {
            anyMR = response.anyMR;
        }
    } else if(rule.rules != null) {
        for(var i = 0; i < rule.rules.length; i++) {
            response = parseRuleData(rule.rules[i]);
            modules = modules.concat(response.modules);
            courses = courses.concat(response.courses);
            if(anyCUR == null) {
                anyCUR = response.anyCUR;
            }
            if(anyMR == null) {
                anyMR = response.anyMR;
            }
        }
    } else {
        if(rule.type == 'ModuleRule') {
            modules.push(rule.moduleGroupId);
        } else if(rule.type == 'CourseUnitRule') {
            courses.push(rule.courseUnitGroupId);
        } else if(rule.type == 'AnyCourseUnitRule') {
            anyCUR = rule;
        } else if(rule.type == 'AnyModuleRule') {
            anyMR = rule;
        } else if(rule.type == 'CreditsRule') {
            creditsRules.push(rule);
        }
    }
    return { modules: modules, courses: courses, anyCUR: anyCUR, anyMR: anyMR, creditsRules: creditsRules}
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
        rules.push(<li key={'l-' + rule.localId}><ElementList key={rule.localId} id={rule.localId} ids={subModIds} rule={rule}/></li>);
    } else if(rule.type == 'ModuleRule') {
        var mods = [];
        mods.push(rule.moduleGroupId);
        rules.push(<li key={'l-' + rule.localId}><ElementList key={rule.localId} id={rule.localId} ids={mods} rule={rule}/></li>);
    }
    return rules;
}

function getElementStructure(struct, lv) {
    var structures = [];
    for(var property in struct) {
        if(property.startsWith("phase") && struct[property] != null)  {
            if(property.indexOf("phase1") >= 0
                || qs['allPhases'] == 'true') {
                var phase = struct[property];
                var options = [];
                for (var i = 0; i < phase.options.length; i++) {
                    options.push(phase.options[i].moduleGroupId);
                }
                structures.push(<ul key={property}>
                    <li>{phase.name.fi}</li>
                    <ElementList key={'opt-' + property} id={'opt-' + property} ids={options} lv={lv} rule="{}"/>
                </ul>)
            }
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
                (<b>{node.credits.min}-{node.credits.max}</b>)}<b>op</b>)</li>);
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
