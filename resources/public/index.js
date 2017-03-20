function http_get(url, callback) {
    var xml_http = new XMLHttpRequest();
    xml_http.onreadystatechange = function() {
        if (xml_http.readyState == 4 && xml_http.status == 200)
            callback(xml_http.responseText);
    }
    xml_http.open('GET', url, true);
    xml_http.send(null);
}

function create_link_list_item(link) {
    var a = document.createElement("a");
    a.text = link;
    a.href = link;
    var li = document.createElement('li');
    li.appendChild(a);
    return li;
}

function run_query(q) {
    http_get('/query/' + q, function(data) {
        var result_list = document.getElementById("result-list");
        links = JSON.parse(data);
        tags = [];
        result_list.innerHTML = ''
        for (var i = 0; i < links.length && i < 10; i++) {
            var tag = create_link_list_item(links[i]);
            result_list.appendChild(tag);
        }
    });
}

var query_field = document.getElementById('query-field');
query_field.onchange = function(e) {
    run_query(e.target.value);
}
