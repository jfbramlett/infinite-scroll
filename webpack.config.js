var path = require('path');

var node_dir = './node_modules';

module.exports = {
    entry: './src/main/webapp/index.jsx',
    devtool: 'sourcemaps',
    cache: true,
    output: {
        filename: './src/main/resources/static/built/bundle.js'
    },
    resolve: {
      extensions: ['.js', '.jsx']
    },
    module: {
        loaders: [
            {
                test: path.join(__dirname, '.'),
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    cacheDirectory: true,
                    presets: ['es2015', 'react'],
                    plugins: ['transform-class-properties']
                }
            }
        ]
    }
};