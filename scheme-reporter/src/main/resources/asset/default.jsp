<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="enums" type="java.util.List<org.jooq.util.EnumDefinition>"--%>
<%--@elvariable id="sequences" type="java.util.List<org.jooq.util.SequenceDefinition>"--%>
<%--@elvariable id="tables" type="java.util.List<org.jooq.util.TableDefinition>"--%>

<%--@elvariable id="totalRelationSvg" type="java.lang.String"--%>
<%--@elvariable id="relationSvg" type="java.util.Map<java.lang.String,java.lang.String>"--%>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Report</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="">
    <link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"/>

    <style type="text/css">
        .element-in-set:after {
            content: ', ';
        }
        .element-in-set:last-of-type:after {
            content: '';
        }
        .text-ellipsis {
            text-overflow: ellipsis;
            white-space: nowrap;
            overflow: hidden;
            width: 100%;
        }
        .svg-container svg {
            width: 100%;
        }
    </style>
</head>
<body>
<div class="container">
    <c:if test="${enums.size() > 0}">
        <a name="EnumDefinition"></a>
        <h3> Enum Definition </h3>
        <c:forEach var="enumElement" items="${enums}" varStatus="status">
            <a name="enum$${enumElement.name}"></a>
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4>${enumElement.name}<small><i>${enumElement.comment}</i></small></h4>
                </div>
                <table class="table table-condensed" style="table-layout: fixed;">
                    <tbody>
                    <tr>
                        <td>
                            <c:forEach var="literal" items="${enumElement.literals}">
                                <span class="element-in-set">${literal}</span>
                            </c:forEach>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </c:forEach>
    </c:if>

    <a name="TableDefinition"></a>
    <h3> Table Definition </h3>
    <div class="svg-container">${totalRelationSvg}</div>
    <c:forEach var="tableElement" items="${tables}" varStatus="status">
        <a name="table$${tableElement.name}"></a>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4>${tableElement.name}<small><i>${tableElement.comment}</i></small></h4>
                <div style="text-align: center;">${relationSvg.get(tableElement.name)}</div>
            </div>
            <table class="table table-condensed" style="table-layout: fixed;">
                <colgroup>
                    <col style="width: 20%"/>
                    <col style="width: 20%"/>
                    <col style="width: 6%"/>
                    <col style="width: 6%"/>
                    <col style="width: 8%"/>
                    <col style="width: 20%"/>
                    <col style="width: 20%"/>
                </colgroup>
                <thead>
                <tr class="info">
                    <th>column</th>
                    <th>type</th>
                    <th style="text-align: center;">nullable</th>
                    <th style="text-align: center;">pkey</th>
                    <th style="text-align: center;">defaulted</th>
                    <th>referred</th>
                    <th>refer</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="column" items="${tableElement.columns}">
                    <tr>
                        <td class="text-ellipsis" title="${column.name}">${column.name}</td>
                        <c:choose>
                            <c:when test="${column.type.type.equals('USER-DEFINED')}">
                                <td title="${column.type.userType}" onclick="location.href='#enum$${column.type.userType}'" role="button" class="text-ellipsis text-info">${column.type.userType}</td>
                            </c:when>
                            <c:otherwise>
                                <td title="${column.type.type}${column.type.length != 0 ? ' ('.concat(column.type.length).concat(')') : ''}" class="text-ellipsis">${column.type.type}${column.type.length != 0 ? ' ('.concat(column.type.length).concat(')') : ''}</td>
                            </c:otherwise>
                        </c:choose>
                        <td style="text-align: center;">
                            <c:if test="${column.nullable}">
                                <div class="glyphicon glyphicon-ok"></div>
                            </c:if>
                        </td>
                        <td style="text-align: center;">
                            <c:if test="${column.primaryKey != null}">
                                <div class="glyphicon glyphicon-ok"></div>
                            </c:if>
                        </td>
                        <td style="text-align: center;">
                            <c:if test="${column.type.defaulted}">
                                <div class="glyphicon glyphicon-ok"></div>
                            </c:if>
                        </td>
                        <td>
                            <c:forEach var="key" items="${column.uniqueKeys}">
                                <c:forEach var="fkey" items="${key.foreignKeys}">
                                    <div class="text-ellipsis" title="[${fkey.keyTable.name}]<c:forEach var="col" items="${fkey.keyColumns}"> ${col.name}</c:forEach>"> [${fkey.keyTable.name}]<c:forEach var="col" items="${fkey.keyColumns}"> ${col.name}</c:forEach> </div>
                                </c:forEach>
                            </c:forEach>
                        </td>
                        <td>
                            <c:forEach var="fkey" items="${column.foreignKeys}">
                                <div>
                                    <a href="#key$${fkey.referencedKey.name}" class="text-ellipsis"
                                       title="[${fkey.referencedTable.name}]<c:forEach var="col" items="${fkey.referencedColumns}"> ${col.name}</c:forEach>">
                                        [${fkey.referencedTable.name}]<c:forEach var="col" items="${fkey.referencedColumns}"> ${col.name}</c:forEach>
                                    </a>
                                </div>
                            </c:forEach>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <c:if test="${tableElement.uniqueKeys.size() > 0}">
                <table class="table table-condensed" style="table-layout: fixed;">
                    <colgroup>
                        <col style="width: 20%"/>
                        <col style="width: 30%"/>
                        <col style="width: 50%"/>
                    </colgroup>
                    <thead>
                    <tr class="info">
                        <th>unique key</th>
                        <th>columns</th>
                        <th>description</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="ukey" items="${tableElement.uniqueKeys}">
                        <tr>
                            <td class="text-ellipsis" title="${ukey.name}">${ukey.name}<a name="key$${ukey.name}"></a></td>
                            <td><c:forEach var="e" items="${ukey.keyColumns}"><span class="element-in-set">${e.name}</span></c:forEach></td>
                            <td>${ukey.comment}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
            <c:if test="${tableElement.columns.stream().filter(e -> e.comment != null && e.comment.length() > 0).count() > 0}">
                <table class="table table-condensed" style="table-layout: fixed;">
                    <colgroup>
                        <col style="width: 20%"/>
                        <col style="width: 80%"/>
                    </colgroup>

                    <thead>
                    <tr class="info">
                        <th>column</th>
                        <th>description</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="column" items="${tableElement.columns}">
                        <c:if test="${column.comment != null && column.comment.length() > 0 }">
                            <tr>
                                <td class="text-ellipsis" title="${column.name}">${column.name}</td>
                                <td>${column.comment}</td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
    </c:forEach>
</div>

<script type="text/javascript">
    var svgs = document.querySelectorAll('svg');
    for (var i = 0; i < svgs.length; i++)
        svgs[i].style.maxWidth = '100%';

	var polygons = document.querySelectorAll('svg > g > polygon');
    for (i = 0; i < polygons.length; i++)
        polygons[i].setAttribute('fill', 'transparent');
</script>
</body>
</html>