function navigate(anchor) {
    const main = document.getElementById("main")
    const iframes = Array.from(main.querySelectorAll("iframe"))
    const matchingIframes = iframes.filter(iframe => iframe.name === anchor.href)
    let iframe
    if(matchingIframes.length == 0) {
        iframe = document.createElement('iframe')
        iframe.src = anchor.href
        iframe.name = anchor.href
        iframe.style.display = 'none'
        main.appendChild(iframe)
    } else iframe = matchingIframes[0]

    iframes.forEach(iframe => { iframe.style.display = "none" })
    iframe.style.display = 'block'

    const sidenav = document.getElementById("sidenav")
    const anchors = Array.from(sidenav.querySelectorAll("a"))
                
    anchors.forEach(anchor => { anchor.classList.remove("selected") })
    anchor.classList.add("selected")
    return false
}

function resetMain() {
    const main = document.getElementById("main")
    main.innerHTML = '';
}

function toggleSideBar(parent, switcher, defWidth) {
    let rootStyle = window.getComputedStyle(document.body)
    let width = rootStyle.getPropertyValue('--sidebar-width').trim()
    if(switcher.classList.contains("mini")) {
        switcher.classList.remove("mini")
        parent.classList.remove("mini")
        width = defWidth
    }
    else {
        switcher.classList.add("mini")
        parent.classList.add("mini")
        width = 'var(--sidebar-switcher-width)'
    }
    document.body.style.setProperty('--sidebar-width', width)
}

function adaptSideBarWidth() {
    let sidenav = document.getElementById("sidenav")
    let width = document.getElementById("width")
    let sidenavW = sidenav.offsetWidth
    var r = document.querySelector(':root')
    r.style.setProperty('--sidebar-width', (sidenavW+5)+'px')
}

window.onload = (event) => {
    const sidenav = document.getElementById("sidenav")
    const anchors = Array.from(sidenav.querySelectorAll("a"))
    anchors.forEach(anchor => {
        console.log(anchor.href)
        anchor.target = anchor.href
        anchor.onclick = function() { return navigate(this); }
    })

    adaptSideBarWidth()

    let rootStyle = window.getComputedStyle(document.body)
    let defWidth = rootStyle.getPropertyValue('--sidebar-width').trim()

    let switcher = document.createElement("div")
    switcher.addEventListener('click', (event) => { toggleSideBar(sidenav, switcher, defWidth) })
    switcher.setAttribute("id", "sidenav-switcher")
    sidenav.appendChild(switcher)
    navigate(anchors[0])
}