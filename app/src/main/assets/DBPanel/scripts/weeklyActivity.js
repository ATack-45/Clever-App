import { API } from "./api.js";

const gradient = window['chartjs-plugin-gradient'];

const API_CLIENT = new API("https://student-tracker-api.azurewebsites.net", "NFJejnqGdi" )

const barLinePlugin = {
    id: 'barLinePlugin',
    afterDatasetsDraw(chart) {
        const { ctx, chartArea: { top, bottom }, scales: { x, y } } = chart;

        // Set the style for the line
        ctx.strokeStyle = 'rgb(66, 255, 132)';
        ctx.lineWidth = 2;

        chart.data.datasets.forEach((dataset, datasetIndex) => {
            const meta = chart.getDatasetMeta(datasetIndex);

            meta.data.forEach((bar, index) => {
                const barTop = bar.y;
                const barLeft = bar.x - bar.width / 2;
                const barRight = bar.x + bar.width / 2;

                // Draw a line across the top of each bar
                ctx.beginPath();
                ctx.moveTo(barLeft, barTop);
                ctx.lineTo(barRight, barTop);
                ctx.stroke();
            });
        });
    }
};

var data = [
    { Day: "Mon", Punchouts: 0 },
    { Day: "Tue", Punchouts: 0 },
    { Day: "Wed", Punchouts: 0 },
    { Day: "Thu", Punchouts: 0 },
    { Day: "Fri", Punchouts: 0 },
];

var mostRecentData = {}

const ctx = document.getElementById('weeklyActivity').getContext('2d');

let myChart = new Chart(ctx, {
    type: 'bar',
    data: {
        labels: data.map(row => row.Day),
        datasets: [
            {
                label: 'Weekly Punchouts',
                data: data.map(row => row.Punchouts),
                tension: 0.1,
                borderColor: 'rgb(66, 255, 132)',
                borderWidth: {
                    top: 2,
                    right: 0,
                    bottom: 0,
                    left: 0
                },
                gradient: {
                    backgroundColor: {
                        axis: 'y',
                        colors: {
                            0: 'rgb(28, 28, 31)',
                            100: 'rgb(50, 186, 98)'
                        }
                    },
                }
            }
        ]
    },
    options: {
        plugins: {
            title: {
                display: false
            },
            legend: {
                display: false
            },
            datalabels: {
                anchor: 'start',
                align: 'top',
                formatter: Math.round,
                color: '#fff',
                offset: -6,
                font: {
                    weight: 'bold'
                }
            },
            tooltip: {
                enabled: false
            }
        },
        scales: {
            y: {
                display: false,
                grid: {
                    display: false
                }
            },
            x: {
                grid: {
                    display: false
                }
            }
        },
        responsive: true,
        animation: {
            duration: 250,
        },
        transitions: {
            active: {
                animation: {
                    duration: 250
                }
            }
        },
    },
    plugins: [ChartDataLabels, gradient]
});


// setInterval(() => {
    // const newData = [
    //     { Day: "M", Punchouts: Math.floor(Math.random() * 10) + 1 },
    //     { Day: "T", Punchouts: Math.floor(Math.random() * 10) + 1 },
    //     { Day: "W", Punchouts: Math.floor(Math.random() * 10) + 1 },
    //     { Day: "T", Punchouts: Math.floor(Math.random() * 10) + 1 },
    //     { Day: "F", Punchouts: Math.floor(Math.random() * 10) + 1 },
    // ];

    // myChart.data.datasets[0].data = newData.map(row => row.Punchouts);
    // myChart.update();


// }, 1000);


const btns = Array.from([
    document.getElementById('groupAll'),
    document.getElementById('groupPM'),
    document.getElementById('groupAM')
]);

btns.forEach(e => { e.addEventListener('click', () => { onGroupClicked(e) }) }) 

function onGroupClicked(clicked) {
    console.log("CLICKED")
    btns.forEach(e => {
        e.classList.remove('selected')
    })
    clicked.classList.add('selected')

    var filter = ''
    if (document.getElementById('groupPM').classList.contains('selected'))
        filter = 'AM'
    else if (document.getElementById('groupAM').classList.contains('selected'))
        filter = 'PM'

    var filteredData = mostRecentData.filter(item => item.session !== filter); 


    const totals = filteredData.reduce((acc, item) => {
        acc[item.day] = (acc[item.day] || 0) + item.count;
        return acc;
      }, {});

    var newData = [
        { Day: "M", Punchouts: totals['Monday'] || 0 },
        { Day: "T", Punchouts: totals['Tuesday'] || 0},
        { Day: "W", Punchouts: totals['Wednesday'] || 0},
        { Day: "T", Punchouts: totals['Thursday'] || 0 },
        { Day: "F", Punchouts: totals['Friday'] || 0 }
    ]

    myChart.data.datasets[0].data = newData.map(row => row.Punchouts);
    myChart.update()
}


document.addEventListener('DOMContentLoaded', () => {
    API_CLIENT.getSimpleData().then(resp => resp.json()).then(r => {
        mostRecentData = r;
        console.log(mostRecentData)
        onGroupClicked(document.getElementById('groupAll'))
    })

})